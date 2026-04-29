import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss']
})
export class AdminLayoutComponent {
  sidebarOpen = true;

  navItems = [
    { label: 'Dashboard', icon: 'fa-chart-pie', route: '/admin' },
    { label: 'Users', icon: 'fa-users', route: '/admin/users' },
    { label: 'Posts', icon: 'fa-newspaper', route: '/admin/posts' },
    { label: 'Discussions', icon: 'fa-comments', route: '/admin/discussions' },
    { label: 'Reports', icon: 'fa-flag', route: '/admin/reports' },
    { label: 'Knowledge Base', icon: 'fa-book', route: '/admin/knowledge-base' },
  ];
}
