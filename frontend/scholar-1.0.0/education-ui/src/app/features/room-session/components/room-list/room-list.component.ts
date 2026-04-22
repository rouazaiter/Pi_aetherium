import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RoomSessionService, RoomSession } from '../../services/room-session.service';
import { AppLayoutComponent } from '../app-layout/app-layout.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
  selector: 'app-room-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AppLayoutComponent, SidebarComponent, TopbarComponent],
  template: `
    <app-app-layout>
      <app-topbar roomName="Rooms"></app-topbar>
      <app-sidebar [activeSection]="'code'"></app-sidebar>

      <main layout-main class="flex-1 bg-white dark:bg-slate-900 flex flex-col min-w-0">
        <div class="border-b border-slate-200 dark:border-slate-800 px-6 py-5 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 class="text-h2 text-slate-900 dark:text-white">Room Sessions</h1>
            <p class="text-body-sm text-slate-500 mt-1">Create and join active collaborative rooms</p>
          </div>

          <div class="flex items-center gap-2 w-full md:w-auto">
            <input
              class="bg-transparent border border-slate-300 dark:border-slate-700 rounded-lg px-3 py-2 text-body-md w-full md:w-72"
              type="text"
              name="roomName"
              [(ngModel)]="newRoomName"
              placeholder="Enter room name..."
              (keyup.enter)="createRoom()"
            />
            <button class="py-2.5 bg-primary text-white text-label-md rounded-lg px-4 hover:opacity-90 transition-opacity" (click)="createRoom()">
              Create Room
            </button>
          </div>
        </div>

        <div class="p-6 overflow-auto flex-1">
          <div *ngIf="loading" class="rounded-lg border border-slate-200 dark:border-slate-800 p-6 text-slate-500 text-body-md">Loading active rooms...</div>

          <div *ngIf="error" class="rounded-lg border border-error-container bg-error-container p-4 text-on-error-container text-body-md">
            <p>{{ error }}</p>
            <button class="mt-3 py-2 px-4 bg-primary text-white rounded-lg" (click)="loadActiveRooms()">Retry</button>
          </div>

          <div *ngIf="!loading && !error" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            <button
              *ngFor="let room of activeRooms"
              type="button"
              class="text-left bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-4 rounded-xl hover:border-primary transition-colors"
              (click)="joinRoom(room)">
              <div class="flex items-center justify-between mb-2">
                <h3 class="text-h3 text-slate-900 dark:text-white truncate">{{ room.name }}</h3>
                <span class="text-[11px] px-2 py-1 rounded-full bg-secondary-container text-on-secondary-container">{{ room.status }}</span>
              </div>
              <p class="text-body-sm text-slate-500">Host: User #{{ room.hostUserId }}</p>
              <p class="text-body-sm text-slate-500">Participants: {{ room.participantCount || 0 }}</p>
              <p class="text-body-sm text-slate-500">Started: {{ formatDate(room.startTime) }}</p>
            </button>
          </div>

          <div *ngIf="!loading && !error && activeRooms.length === 0" class="rounded-lg border border-slate-200 dark:border-slate-800 p-8 text-center text-slate-500 text-body-md">
            No active rooms. Create one to get started.
          </div>
        </div>
      </main>

      <aside layout-right class="w-[320px] bg-slate-50 dark:bg-slate-950 border-l border-slate-200 dark:border-slate-800 p-4 hidden xl:block">
        <h3 class="font-label-md text-xs text-slate-500 uppercase tracking-wider">Quick Tips</h3>
        <div class="mt-3 space-y-3 text-body-sm text-slate-600 dark:text-slate-300">
          <p>- Create a room with a clear topic.</p>
          <p>- Join an ACTIVE room to open IDE, whiteboard, video, and chat.</p>
          <p>- Share room ID with collaborators to sync instantly.</p>
        </div>
      </aside>
    </app-app-layout>
  `
})
export class RoomListComponent implements OnInit, OnDestroy {
  activeRooms: any[] = [];
  newRoomName = '';
  userId = 0;
  loading = true;
  error = '';

  private refreshInterval: any;

  constructor(
    private roomSessionService: RoomSessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const storedUserId = localStorage.getItem('userId');
    this.userId = storedUserId ? +storedUserId : 1;
    this.loadActiveRooms();

    this.refreshInterval = setInterval(() => {
      this.loadActiveRooms();
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  createRoom(): void {
    const roomName = this.newRoomName?.trim();
    if (!roomName) {
      alert('Please enter a room name');
      return;
    }

    this.roomSessionService.createRoom(roomName, this.userId).subscribe({
      next: (room) => {
        this.router.navigate(['/rooms', room.id]);
      },
      error: (err) => {
        alert('Error creating room: ' + (err.message || err.statusText || 'Unknown error'));
      }
    });
  }

  joinRoom(room: RoomSession): void {
    this.router.navigate(['/rooms', room.id]);
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleString();
  }

  loadActiveRooms(): void {
    this.loading = true;
    this.error = '';
    this.roomSessionService.getActiveRooms().subscribe({
      next: (rooms) => {
        this.activeRooms = rooms;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to connect to server. Make sure backend is running on port 8080.';
        this.loading = false;
      }
    });
  }
}