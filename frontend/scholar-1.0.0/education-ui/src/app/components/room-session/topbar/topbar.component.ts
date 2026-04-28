import { Component, Input, OnInit, OnChanges, Inject, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss']
})
export class TopbarComponent implements OnInit, OnChanges {
  @Input() roomName = '';
  @Input() isRecording = false;
  @Input() recordingDuration = 0;
  @Output() recordingToggle = new EventEmitter<void>();
  isDarkMode = false;
  private recordingInterval: ReturnType<typeof setInterval> | null = null;
  localDuration = 0;

  constructor(@Inject(DOCUMENT) private document: Document) {}

  ngOnInit(): void {
    const saved = localStorage.getItem('ui-theme');
    if (!saved) {
      localStorage.setItem('ui-theme', 'light');
    }
    this.isDarkMode = saved ? saved === 'dark' : false;
    this.applyTheme();
    if (this.isRecording && this.recordingDuration > 0) {
      this.localDuration = this.recordingDuration;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['recordingDuration'] && !changes['recordingDuration'].firstChange) {
      this.localDuration = this.recordingDuration;
    }
    if (changes['isRecording'] && !changes['isRecording'].firstChange) {
      if (!this.isRecording) {
        this.localDuration = 0;
      }
    }
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('ui-theme', this.isDarkMode ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme(): void {
    this.document.documentElement.classList.toggle('dark', this.isDarkMode);
  }

  formatDuration(seconds: number): string {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    if (hrs > 0) {
      return `${hrs}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  ngOnDestroy(): void {}
}


