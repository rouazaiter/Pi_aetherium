import { Component, HostListener, inject } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';
import { WelcomeDialogService } from '../../core/services/welcome-dialog.service';

@Component({
  selector: 'app-welcome-dialog',
  standalone: true,
  templateUrl: './welcome-dialog.component.html',
  styleUrl: './welcome-dialog.component.scss',
  animations: [
    trigger('backdrop', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('280ms ease-out', style({ opacity: 1 })),
      ]),
      transition(':leave', [animate('200ms ease-in', style({ opacity: 0 }))]),
    ]),
    trigger('panel', [
      transition(':enter', [
        style({ opacity: 0, transform: 'scale(0.94) translateY(16px)' }),
        animate(
          '420ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ opacity: 1, transform: 'none' }),
        ),
      ]),
      transition(':leave', [
        animate(
          '220ms ease-in',
          style({ opacity: 0, transform: 'scale(0.96) translateY(8px)' }),
        ),
      ]),
    ]),
  ],
})
export class WelcomeDialogComponent {
  protected readonly welcome = inject(WelcomeDialogService);

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.welcome.visible()) {
      this.welcome.close();
    }
  }

  dismiss(): void {
    this.welcome.close();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('welcome-dialog__backdrop')) {
      this.welcome.close();
    }
  }
}

