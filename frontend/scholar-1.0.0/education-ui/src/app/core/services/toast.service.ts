import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  type: ToastType;
  message: string;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  toasts$ = new Subject<Toast>();

  success(message: string, duration = 4000): void { this.emit('success', message, duration); }
  error(message: string, duration = 5000): void { this.emit('error', message, duration); }
  info(message: string, duration = 4000): void { this.emit('info', message, duration); }
  warning(message: string, duration = 4000): void { this.emit('warning', message, duration); }

  private emit(type: ToastType, message: string, duration: number): void {
    this.toasts$.next({ id: ++this.counter, type, message, duration });
  }
}
