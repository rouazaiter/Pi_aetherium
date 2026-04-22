import { Component, Input, OnInit, OnChanges, Inject, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="bg-white border-b border-slate-200 shadow-sm h-16 flex items-center justify-between px-6 w-full z-50 fixed top-0 left-0">
      <div class="flex items-center gap-8 min-w-0">
        <span class="text-lg font-bold text-slate-900">SkillHub</span>
        <div class="relative w-96 hidden md:block">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
          <input class="w-full bg-slate-50 border border-slate-200 rounded-lg py-1.5 pl-10 pr-4 text-body-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="Search team, projects or docs..." type="text" />
        </div>
      </div>
      <div class="flex items-center gap-4">
        <button
          type="button"
          (click)="recordingToggle.emit()"
          [title]="isRecording ? 'Stop Recording' : ''"
          class="px-3 py-1.5 text-xs font-semibold border text-slate-700 hover:bg-slate-100 transition-colors rounded-lg flex items-center gap-1.5"
          [class.border-red-200]="isRecording"
          [class.bg-red-50]="isRecording"
          [class.text-red-600]="isRecording"
          [class.border-slate-200]="!isRecording">
          <span class="material-symbols-outlined text-base" [class.text-red-600]="isRecording" [class.text-slate-600]="!isRecording">
            radio_button_checked
          </span>
          <span>{{ isRecording ? 'REC ' + formatDuration(localDuration) : 'Start Recording' }}</span>
        </button>
        <button
            type="button"
            (click)="toggleTheme()"
            class="p-2 text-slate-600 hover:bg-slate-100 transition-colors rounded-lg"
            [title]="isDarkMode ? 'Passer en mode clair' : 'Passer en mode sombre'">
          <span class="material-symbols-outlined">{{ isDarkMode ? 'light_mode' : 'dark_mode' }}</span>
        </button>
        <button class="p-2 text-slate-600 hover:bg-slate-100 transition-colors rounded-lg">
          <span class="material-symbols-outlined">settings</span>
        </button>
        <button class="p-2 text-slate-600 hover:bg-slate-100 transition-colors rounded-lg">
          <span class="material-symbols-outlined">help</span>
        </button>
        <button class="p-2 text-slate-600 hover:bg-slate-100 transition-colors rounded-lg">
          <span class="material-symbols-outlined">more_vert</span>
        </button>
        <div class="h-8 w-px bg-slate-200 mx-1"></div>
        <div class="text-xs md:text-sm text-slate-600 max-w-[220px] truncate">{{ roomName || 'Workspace' }}</div>
      </div>
    </header>
  `
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


