import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

// Extend Window to include browser speech API
declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

@Injectable({ providedIn: 'root' })
export class SpeechService {

  private recognition: any = null;
  private _listening = false;

  transcript$ = new Subject<string>();
  error$ = new Subject<string>();
  listening$ = new Subject<boolean>();

  get isSupported(): boolean {
    return !!(window.SpeechRecognition || window.webkitSpeechRecognition);
  }

  get isListening(): boolean { return this._listening; }

  start(lang = 'en-US'): void {
    if (!this.isSupported) {
      this.error$.next('Speech recognition is not supported in this browser. Try Chrome.');
      return;
    }
    if (this._listening) { this.stop(); return; }

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    this.recognition = new SpeechRecognition();
    this.recognition.lang = lang;
    this.recognition.continuous = true;       // keep listening until stopped
    this.recognition.interimResults = true;   // show partial results while speaking

    this.recognition.onstart = () => {
      this._listening = true;
      this.listening$.next(true);
    };

    this.recognition.onresult = (event: any) => {
      let interim = '';
      let final = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const t = event.results[i][0].transcript;
        if (event.results[i].isFinal) final += t;
        else interim += t;
      }
      // Emit final text; interim is just for live feedback
      if (final) this.transcript$.next(final);
    };

    this.recognition.onerror = (event: any) => {
      this._listening = false;
      this.listening$.next(false);
      if (event.error !== 'aborted') {
        this.error$.next(`Mic error: ${event.error}`);
      }
    };

    this.recognition.onend = () => {
      this._listening = false;
      this.listening$.next(false);
    };

    this.recognition.start();
  }

  stop(): void {
    if (this.recognition) {
      this.recognition.stop();
      this.recognition = null;
    }
    this._listening = false;
    this.listening$.next(false);
  }
}
