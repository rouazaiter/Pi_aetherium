import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

@Component({
  selector: 'app-mic-button',
  template: `
    <div class="mic-wrapper">
      <!-- Language selector — shown when not listening -->
      <select class="mic-lang-select" [(ngModel)]="selectedLang" *ngIf="!listening" title="Dictation language">
        <option *ngFor="let l of langs" [value]="l.code">{{ l.flag }}</option>
      </select>

      <!-- Mic button -->
      <button
        type="button"
        class="mic-btn"
        [class.listening]="listening"
        [class.unsupported]="!supported"
        (click)="toggle()"
        [title]="tooltip">
        <i class="fa" [class.fa-microphone]="!listening" [class.fa-stop]="listening"></i>
        <span class="mic-pulse" *ngIf="listening"></span>
      </button>

      <span class="mic-error" *ngIf="errorMsg">{{ errorMsg }}</span>
    </div>
  `,
  styles: [`
    .mic-wrapper { display: inline-flex; align-items: center; gap: 6px; }

    .mic-lang-select {
      padding: 4px 6px; border: 1px solid #e0e0e0; border-radius: 8px;
      font-size: 14px; outline: none; cursor: pointer; background: #fff;
      font-family: 'Poppins', sans-serif; height: 36px;
      &:focus { border-color: var(--primary); }
    }

    .mic-btn {
      position: relative; width: 36px; height: 36px; border-radius: 50%;
      border: 2px solid var(--primary); background: #fff; color: var(--primary);
      cursor: pointer; font-size: 15px; display: flex; align-items: center;
      justify-content: center; transition: all .2s; flex-shrink: 0;
      &:hover { background: var(--primary); color: #fff; }
      &.listening { background: #e53935; border-color: #e53935; color: #fff; }
      &.unsupported { opacity: 0.4; cursor: not-allowed; }
    }

    .mic-pulse {
      position: absolute; inset: -4px; border-radius: 50%;
      border: 2px solid #e53935; animation: pulse 1.2s ease-out infinite;
    }

    @keyframes pulse {
      0% { transform: scale(1); opacity: 1; }
      100% { transform: scale(1.7); opacity: 0; }
    }

    .mic-error { font-size: 11px; color: #e53935; }
  `],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class MicButtonComponent implements OnDestroy {

  @Output() textReceived = new EventEmitter<string>();

  readonly langs = [
    { code: 'en-US', flag: '🇺🇸 EN' },
    { code: 'fr-FR', flag: '🇫🇷 FR' },
    { code: 'ar-SA', flag: '🇸🇦 AR' }
  ];

  selectedLang = 'en-US';
  listening = false;
  errorMsg = '';
  supported = !!(window.SpeechRecognition || window.webkitSpeechRecognition);

  private recognition: any = null;

  get tooltip(): string {
    if (!this.supported) return 'Speech recognition not supported (use Chrome)';
    return this.listening ? 'Click to stop recording' : `Dictate in ${this.selectedLang}`;
  }

  toggle(): void {
    if (!this.supported) return;
    this.listening ? this.stop() : this.start();
  }

  private start(): void {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    this.recognition = new SR();
    this.recognition.lang = this.selectedLang;
    this.recognition.continuous = true;
    this.recognition.interimResults = false;

    this.recognition.onstart = () => { this.listening = true; };

    this.recognition.onresult = (event: any) => {
      let final = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        if (event.results[i].isFinal) final += event.results[i][0].transcript;
      }
      if (final.trim()) this.textReceived.emit(final.trim());
    };

    this.recognition.onerror = (event: any) => {
      this.listening = false;
      if (event.error !== 'aborted') {
        this.errorMsg = `Mic error: ${event.error}`;
        setTimeout(() => this.errorMsg = '', 4000);
      }
    };

    this.recognition.onend = () => { this.listening = false; };
    this.recognition.start();
  }

  private stop(): void {
    if (this.recognition) { this.recognition.stop(); this.recognition = null; }
    this.listening = false;
  }

  ngOnDestroy(): void { this.stop(); }
}
