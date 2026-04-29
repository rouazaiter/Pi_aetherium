import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastComponent } from '../shared/toast/toast.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ToastComponent]
})
export class LayoutComponent implements OnInit, OnDestroy {

  constructor(public auth: AuthService, private router: Router) {}

  get isAdmin(): boolean {
    return this.auth.auth()?.role === 'admin';
  }

  ngOnInit(): void {
    document.body.style.overflow = 'auto';
  }

  ngOnDestroy(): void {
    document.body.style.overflow = 'hidden';
  }
}
