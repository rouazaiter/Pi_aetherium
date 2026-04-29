import {
  Component, OnInit, OnDestroy, HostListener,
  ElementRef, ViewChild, ViewEncapsulation, Renderer2
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ConfirmModalService, ConfirmOptions } from './confirm-modal.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-confirm-modal',
  templateUrl: './confirm-modal.component.html',
  styleUrls: ['./confirm-modal.component.scss'],
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  imports: [NgIf]
})
export class ConfirmModalComponent implements OnInit, OnDestroy {

  visible = false;
  closing = false;

  title         = '';
  message       = '';
  confirmLabel  = 'Confirm';
  cancelLabel   = 'Cancel';
  variant: ConfirmOptions['variant'] = 'danger';

  @ViewChild('confirmBtn') confirmBtn!: ElementRef<HTMLButtonElement>;
  @ViewChild('backdropEl') backdropEl!: ElementRef<HTMLElement>;

  private resolve!: (v: boolean) => void;
  private sub!: Subscription;

  constructor(
    private svc: ConfirmModalService,
    private el: ElementRef,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    // Move this component's host element directly into <body>
    // so position:fixed is relative to the viewport, not any overflow:hidden ancestor
    this.renderer.appendChild(document.body, this.el.nativeElement);

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

  onBackdropAnimated(): void {
    setTimeout(() => this.confirmBtn?.nativeElement?.focus(), 50);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void { if (this.visible) this.answer(false); }

  answer(value: boolean): void {
    this.closing = true;
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
