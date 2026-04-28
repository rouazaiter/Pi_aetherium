import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-layout',
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
