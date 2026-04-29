import { Component, OnInit, OnDestroy } from '@angular/core';
import { Toast, ToastService } from '../toast.service';
import { Subscription } from 'rxjs';
import { NgFor } from '@angular/common';

@Component({
    selector: 'app-toast',
    template: `
    <div class="toast-container">
      <div *ngFor="let t of toasts" class="toast toast-{{t.type}}" [@fadeSlide]>
        <span class="toast-icon">{{ icons[t.type] }}</span>
        <span class="toast-msg">{{ t.message }}</span>
        <button class="toast-close" (click)="remove(t.id)">✕</button>
      </div>
    </div>`,
    styleUrls: ['./toast.component.scss'],
    animations: [],
    standalone: true,
    imports: [NgFor]
})
export class ToastComponent implements OnInit, OnDestroy {
  toasts: Toast[] = [];
  icons = { success: '✅', error: '❌', info: 'ℹ️' };
  private subs = new Subscription();

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.subs.add(this.toastService.toasts$.subscribe(t => this.toasts.push(t)));
    this.subs.add(this.toastService.dismiss$.subscribe(id => this.remove(id)));
  }

  remove(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }
  ngOnDestroy(): void { this.subs.unsubscribe(); }
}
