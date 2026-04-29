import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RoomSessionService, RoomSession } from '../../../core/services/room-session/room-session.service';
import { AppLayoutComponent } from '../app-layout/app-layout.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
  selector: 'app-room-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AppLayoutComponent, SidebarComponent, TopbarComponent],
  templateUrl: './room-list.component.html',
  styleUrls: ['./room-list.component.scss']
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