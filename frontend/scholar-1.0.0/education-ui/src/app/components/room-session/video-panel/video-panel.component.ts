import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import DailyIframe from '@daily-co/daily-js';

const DAILY_DEMO_ROOM_URL = 'https://demo.daily.co/hello';
const DAILY_ROOM_URL_STORAGE_KEY = 'dailyRoomUrl';

@Component({
  selector: 'app-video-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-panel.component.html',
  styleUrls: ['./video-panel.component.scss']
})
export class VideoPanelComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() roomId: number = 0;
  @Input() userId: number = 0;
  @Input() isHost: boolean = false;
  @Input() userName: string = 'You';
  @Input() dailyRoomUrl: string = '';
  @ViewChild('videoContainer') videoContainer!: ElementRef<HTMLDivElement>;

  error = '';
  usingFallbackRoom = false;
  private callFrame: any = null;

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    void this.initDailyCall();
  }

  async ngOnDestroy(): Promise<void> {
    if (this.callFrame) {
      try {
        await this.callFrame.leave();
      } catch {
        // ignore leave error during teardown
      }
      this.callFrame.destroy();
      this.callFrame = null;
    }
  }

  private async initDailyCall(): Promise<void> {
    try {
      const roomUrl = this.resolveRoomUrl();
      if (!roomUrl) {
        this.error = 'Daily room URL is missing. Set a URL in localStorage key "dailyRoomUrl".';
        return;
      }

      if (!this.videoContainer?.nativeElement) {
        this.error = 'Video container is not ready.';
        return;
      }

      this.callFrame = DailyIframe.createFrame(this.videoContainer.nativeElement, {
        showLeaveButton: false,
        showFullscreenButton: true,
        iframeStyle: {
          width: '100%',
          height: '100%',
          border: '0',
          borderRadius: '12px'
        }
      });

      this.callFrame.on('error', (event: any) => {
        this.error = event?.errorMsg || 'Daily connection error.';
      });

      await this.callFrame.join({
        url: roomUrl,
        userName: this.userName || `User ${this.userId}`,
        startVideoOff: false,
        startAudioOff: false
      });
    } catch (err: any) {
      this.error = err?.message || 'Unable to join Daily room.';
    }
  }

  private resolveRoomUrl(): string {
    const inputUrl = this.dailyRoomUrl?.trim();
    if (inputUrl) {
      this.usingFallbackRoom = false;
      return inputUrl;
    }
    const storedUrl = localStorage.getItem(DAILY_ROOM_URL_STORAGE_KEY)?.trim() || '';
    if (storedUrl) {
      this.usingFallbackRoom = false;
      return storedUrl;
    }
    this.usingFallbackRoom = true;
    return DAILY_DEMO_ROOM_URL;
  }

  openRoomSettings(): void {
    const current = this.dailyRoomUrl?.trim() || localStorage.getItem(DAILY_ROOM_URL_STORAGE_KEY) || DAILY_DEMO_ROOM_URL;
    const next = window.prompt('Paste Daily room URL', current);
    if (!next || !next.trim()) {
      return;
    }
    localStorage.setItem(DAILY_ROOM_URL_STORAGE_KEY, next.trim());
    window.location.reload();
  }
}