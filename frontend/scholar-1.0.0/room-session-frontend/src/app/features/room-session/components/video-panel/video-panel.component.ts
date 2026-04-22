import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

declare global {
  interface Window {
    DailyIframe: any;
  }
}

@Component({
  selector: 'app-video-panel',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="h-full bg-slate-200 dark:bg-slate-800 p-px overflow-auto relative">
      <div #videoContainer class="w-full h-full"></div>
    </section>
  `
})
export class VideoPanelComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() roomId: number = 0;
  @Input() userId: number = 0;
  @Input() isHost: boolean = false;
  @Input() userName: string = 'You';
  @Input() dailyRoomUrl: string = '';

  @ViewChild('videoContainer') videoContainer!: ElementRef<HTMLDivElement>;

  error = '';
  private callFrame: any = null;

  private destroy$ = new Subject<void>();

  constructor() {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    void this.initDailyCall();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.callFrame) {
      this.callFrame.destroy();
    }
  }

  private async initDailyCall(): Promise<void> {
    if (!this.dailyRoomUrl) {
      this.error = 'Daily room URL not configured';
      return;
    }

    if (!window.DailyIframe) {
      const script = document.createElement('script');
      script.src = 'https://cdn.daily.co/daily-js/v0.62.0/daily-iframe.js';
      script.onload = () => this.createCallFrame();
      script.onerror = () => {
        this.error = 'Failed to load Daily.co SDK';
      };
      document.head.appendChild(script);
    } else {
      this.createCallFrame();
    }
  }

  private createCallFrame(): void {
    if (!this.videoContainer?.nativeElement || !window.DailyIframe) {
      return;
    }

    this.callFrame = window.DailyIframe.createFrame(this.videoContainer.nativeElement, {
      iframeStyle: {
        width: '100%',
        height: '100%',
        border: 'none',
        borderRadius: '8px',
      },
      showLeaveButton: true,
      userData: {
        userName: this.userName,
      },
    });

    this.callFrame.on('joined-meeting', () => {
      console.log('Joined Daily meeting');
    });

    this.callFrame.on('left-meeting', () => {
      console.log('Left Daily meeting');
    });

    this.callFrame.on('error', (err: any) => {
      console.error('Daily error:', err);
      this.error = err?.errorMsg || 'Daily call error';
    });

    this.callFrame.join({
      url: this.dailyRoomUrl,
    });
  }
}