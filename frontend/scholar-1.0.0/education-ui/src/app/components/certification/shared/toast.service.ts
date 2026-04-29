import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface Toast { id: number; type: 'success' | 'error' | 'info'; message: string; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  private _toasts$ = new Subject<Toast>();
  toasts$ = this._toasts$.asObservable();
  private counter = 0;

  show(type: Toast['type'], message: string, duration = 3500): void {
    const id = ++this.counter;
    this._toasts$.next({ id, type, message });
    setTimeout(() => this._dismiss$.next(id), duration);
  }

  private _dismiss$ = new Subject<number>();
  dismiss$ = this._dismiss$.asObservable();

  success(msg: string) { this.show('success', msg); }
  error(msg: string)   { this.show('error', msg); }
  info(msg: string)    { this.show('info', msg); }
}
