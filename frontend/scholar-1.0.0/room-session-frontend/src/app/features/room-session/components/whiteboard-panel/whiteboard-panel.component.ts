import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { WhiteboardService, WhiteboardConfig, FastboardModule } from '../../services/whiteboard.service';

@Component({
  selector: 'app-whiteboard-panel',
  standalone: true,
  host: {
    class: 'block h-full min-h-0 flex-1'
  },
  imports: [CommonModule],
  template: `
    <section class="h-full flex flex-col bg-white dark:bg-slate-900 relative">
      <div class="p-3 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
        <h4 class="font-label-md text-xs text-slate-500 uppercase tracking-wider">Agora Fastboard</h4>
        <span *ngIf="loading" class="text-[11px] text-slate-500">Connecting...</span>
      </div>

      <div #whiteboardContainer class="flex-1 min-h-0 bg-white"></div>

      <div *ngIf="error" class="px-3 py-2 text-[11px] text-error border-t border-slate-200 dark:border-slate-800">{{ error }}</div>

      <div *ngIf="toastMessage" class="absolute top-12 right-3 max-w-[360px] px-3 py-2 rounded-lg bg-error text-white shadow-lg text-xs" role="status" aria-live="polite">
        {{ toastMessage }}
      </div>
    </section>
  `
})
export class WhiteboardPanelComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() roomId: number = 0;

  @ViewChild('whiteboardContainer') containerRef!: ElementRef<HTMLDivElement>;

  loading = true;
  error = '';
  toastMessage = '';

  private fastboard: any = null;
  private unmountFn: (() => void) | null = null;
  private initialized = false;
  private toastTimer: any = null;

  constructor(private whiteboardService: WhiteboardService) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    void this.initWhiteboard();
  }

  ngOnDestroy(): void {
    if (this.unmountFn) {
      try {
        this.unmountFn();
      } catch (e) {}
      this.unmountFn = null;
    }
    if (this.fastboard) {
      try {
        this.fastboard.destroy?.();
      } catch (e) {}
      this.fastboard = null;
    }
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
  }

  private async initWhiteboard(): Promise<void> {
    if (this.initialized) {
      return;
    }

    if (!this.containerRef?.nativeElement) {
      this.error = 'Container not found';
      this.loading = false;
      return;
    }

    this.loading = true;
    this.error = '';

    try {
      const fastboardModule = await this.whiteboardService.loadFastboard();
      const config = await firstValueFrom(this.whiteboardService.getConfig());
      await this.connectToWhiteboard(fastboardModule, config);
      this.initialized = true;
    } catch (error) {
      const message = this.mapToUserMessage(error);
      this.setError(message, true);
      this.loading = false;
      console.error('Whiteboard initialization error:', error);
    }
  }

  private async connectToWhiteboard(fastboardModule: FastboardModule, config: WhiteboardConfig): Promise<void> {
    try {
      if (!config?.appIdentifier) {
        this.setError('Whiteboard non configure. Ajoutez whiteboard.app-identifier puis redemarrez le backend.', true);
        this.loading = false;
        return;
      }

      const uuid = await this.getMappedOrCreateRoomUuid();

      const tokenData = await firstValueFrom(this.whiteboardService.getRoomToken(
        uuid,
        'writer',
        3600000
      ));

      const roomToken = tokenData?.roomToken;
      if (!roomToken) {
        this.setError('Room token introuvable. Verifiez la configuration backend whiteboard.', true);
        this.loading = false;
        return;
      }

      this.fastboard = await fastboardModule.createFastboard({
        sdkConfig: {
          appIdentifier: config.appIdentifier,
          region: config.region || 'us-sv'
        },
        joinRoom: {
          uid: 'user-' + this.roomId,
          uuid,
          roomToken
        },
        managerConfig: {
          cursor: true
        }
      });

      const mountResult = fastboardModule.mount(this.fastboard, this.containerRef.nativeElement);
      if (typeof mountResult === 'function') {
        this.unmountFn = mountResult;
      }

      this.loading = false;
      console.log('Fastboard connected successfully');

    } catch (error: any) {
      console.error('Whiteboard connection error:', error);
      const message = this.mapToUserMessage(error);
      this.setError(message, true);
      this.loading = false;
    }
  }

  private async getMappedOrCreateRoomUuid(): Promise<string> {
    try {
      const mapped = await firstValueFrom(this.whiteboardService.getMappedRoom(this.roomId));
      if (mapped?.uuid) {
        return mapped.uuid;
      }
    } catch (error) {
      // 404 is expected when the app room is not mapped yet.
    }

    const roomData = await firstValueFrom(this.whiteboardService.createRoom(`Room-${this.roomId}`));
    const uuid = roomData?.uuid;
    if (!uuid) {
      throw new Error('Failed to create whiteboard room');
    }

    await firstValueFrom(this.whiteboardService.mapRoom(this.roomId, uuid));
    return uuid;
  }

  private mapToUserMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const backendMessage = error.error?.error || error.error?.message || error.message;
      if (typeof backendMessage === 'string' && /app identifier|whiteboard\.app-identifier|whiteboard\.ak/i.test(backendMessage)) {
        return 'Whiteboard non configure. Verifiez whiteboard.app-identifier puis redemarrez le backend.';
      }
      if (typeof backendMessage === 'string' && /whiteboard\.sdk-token|whiteboard\.sk|credentials/i.test(backendMessage)) {
        return 'Credentials whiteboard invalides. Verifiez whiteboard.sdk-token (ou whiteboard.ak/whiteboard.sk).';
      }
      return backendMessage || 'Erreur backend whiteboard';
    }

    const message = (error as any)?.message;
    if (typeof message === 'string' && /appidentifier|app identifier|whiteboard\.app-identifier|whiteboard\.ak/i.test(message)) {
      return 'Whiteboard non configure. Verifiez whiteboard.app-identifier puis redemarrez le backend.';
    }
    if (typeof message === 'string' && /whiteboard\.sdk-token|whiteboard\.sk|credentials/i.test(message)) {
      return 'Credentials whiteboard invalides. Verifiez whiteboard.sdk-token (ou whiteboard.ak/whiteboard.sk).';
    }

    return typeof message === 'string' && message.length > 0
      ? message
      : 'Failed to connect to whiteboard';
  }

  private setError(message: string, withToast: boolean = false): void {
    this.error = message;
    if (withToast) {
      this.showToast(message);
    }
  }

  private showToast(message: string): void {
    this.toastMessage = message;
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }

    this.toastTimer = setTimeout(() => {
      this.toastMessage = '';
      this.toastTimer = null;
    }, 5000);
  }
}