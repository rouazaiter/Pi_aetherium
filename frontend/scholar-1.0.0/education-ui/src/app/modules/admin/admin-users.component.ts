import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {
  users: any[] = [];
  loading = true;
  search = '';

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getUsers().subscribe({
      next: u => { this.users = u; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get filtered(): any[] {
    if (!this.search.trim()) return this.users;
    const q = this.search.toLowerCase();
    return this.users.filter(u => u.username.toLowerCase().includes(q) || u.email.toLowerCase().includes(q));
  }
}
