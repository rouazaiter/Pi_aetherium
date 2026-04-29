import {
  Component, OnInit, OnDestroy, HostListener, ElementRef, ViewChild, AfterViewInit
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ConfirmModalService, ConfirmOptions } from './confirm-modal.service';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-confirm-modal',
    templateUrl: './confirm-modal.component.html',
    styleUrls: ['./confirm-modal.component.scss'],
    standalone: true,
    imports: [NgIf]
})
export class ConfirmModalComponent implements OnInit, OnDestroy, AfterViewInit {

  visible  = false;
  closing  = false;   // triggers close animation before hiding

  title         = '';
  message       = '';
  confirmLabel  = 'Confirm';
  cancelLabel   = 'Cancel';
  variant: ConfirmOptions['variant'] = 'danger';

  @ViewChild('confirmBtn') confirmBtn!: ElementRef<HTMLButtonElement>;

  private resolve!: (v: boolean) => void;
  private sub!: Subscription;

  constructor(private svc: ConfirmModalService) {}

  ngOnInit(): void {
    this.sub = this.svc.request$.subscribe(req => {
      this.title        = req.title;
      this.message      = req.message;
      this.confirmLabel = req.confirmLabel ?? 'Confirm';
      this.cancelLabel  = req.cancelLabel  ?? 'Cancel';
      this.variant      = req.variant      ?? 'danger';
      this.resolve      = req.resolve;
      this.closing      = false;
      this.visible      = true;
    });
  }

  ngAfterViewInit(): void {}

  /** Auto-focus the confirm button when modal opens so Enter/Space works. */
  onBackdropAnimated(): void {
    setTimeout(() => this.confirmBtn?.nativeElement?.focus(), 50);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void { if (this.visible) this.answer(false); }

  answer(value: boolean): void {
    this.closing = true;
    // Wait for close animation then hide
    setTimeout(() => {
      this.visible = false;
      this.closing = false;
      this.resolve(value);
    }, 180);
  }

  get iconMap(): Record<string, string> {
    return { danger: '🗑️', warning: '⚠️', info: 'ℹ️' };
  }

  get icon(): string { return this.iconMap[this.variant ?? 'danger']; }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}
