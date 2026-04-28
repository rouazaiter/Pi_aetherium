import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { Toast, ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div class="toast-item" *ngFor="let t of toasts" [class]="t.type" (click)="dismiss(t.id)">
        <span class="toast-icon">{{ icon(t.type) }}</span>
        <span class="toast-message">{{ t.message }}</span>
        <button class="toast-close">×</button>
        <div class="toast-progress" [style.animation-duration]="t.duration + 'ms'"></div>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed; bottom: 24px; right: 24px; z-index: 99999;
      display: flex; flex-direction: column; gap: 10px; pointer-events: none;
    }
    .toast-item {
      display: flex; align-items: center; gap: 10px;
      min-width: 300px; max-width: 420px; padding: 14px 16px;
      border-radius: 14px; box-shadow: 0 8px 24px rgba(0,0,0,0.18);
      font-family: 'Poppins', sans-serif; font-size: 14px;
      position: relative; overflow: hidden; cursor: pointer;
      pointer-events: all; animation: toastIn .35s cubic-bezier(.21,1.02,.73,1) forwards;
      &.success { background: #1b5e20; color: #fff; }
      &.error   { background: #b71c1c; color: #fff; }
      &.info    { background: #0d47a1; color: #fff; }
      &.warning { background: #e65100; color: #fff; }
    }
    .toast-icon { font-size: 18px; flex-shrink: 0; }
    .toast-message { flex: 1; line-height: 1.4; }
    .toast-close {
      background: none; border: none; color: rgba(255,255,255,0.7);
      font-size: 20px; cursor: pointer; padding: 0 4px; line-height: 1;
      &:hover { color: #fff; }
    }
    .toast-progress {
      position: absolute; bottom: 0; left: 0; height: 3px;
      background: rgba(255,255,255,0.4);
      animation: toastProgress linear forwards;
      width: 100%;
    }
    @keyframes toastIn {
      from { transform: translateX(120%); opacity: 0; }
      to   { transform: translateX(0);    opacity: 1; }
    }
    @keyframes toastProgress {
      from { width: 100%; }
      to   { width: 0%; }
    }
  `]
})
export class ToastComponent implements OnInit, OnDestroy {
  toasts: Toast[] = [];
  private sub!: Subscription;

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.sub = this.toastService.toasts$.subscribe(toast => {
      this.toasts.push(toast);
      setTimeout(() => this.dismiss(toast.id), toast.duration);
    });
  }

  dismiss(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  icon(type: string): string {
    const map: Record<string, string> = {
      success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️'
    };
    return map[type] || 'ℹ️';
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}
