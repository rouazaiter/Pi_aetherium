import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { RoomSessionService, RoomSession } from '../../../core/services/room-session/room-session.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppLayoutComponent } from '../app-layout/app-layout.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-room-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AppLayoutComponent, SidebarComponent, TopbarComponent],
  templateUrl: './room-list.component.html',
  styleUrls: ['./room-list.component.scss']
})
export class RoomListComponent implements OnInit, OnDestroy {
  hostRooms: RoomSession[] = [];
  participantRooms: RoomSession[] = [];
  newRoomName = '';
  joinRoomName = '';
  userId = 0;
  userName = '';
  loading = true;
  error = '';

  private refreshInterval: any;

  constructor(
    private roomSessionService: RoomSessionService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const authUser = this.auth.auth();
    const authUserId = authUser?.userId;
    this.userId = typeof authUserId === 'number' ? authUserId : 0;
    this.userName = authUser?.username || 'User';
    if (!this.userId) {
      this.error = 'Session expirée. Veuillez vous reconnecter.';
      this.loading = false;
      void this.router.navigate(['/login']);
      return;
    }
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
    if (!this.userId) {
      alert('You must be logged in to create a room.');
      void this.router.navigate(['/login']);
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

  joinByRoomName(): void {
    const roomName = this.joinRoomName.trim();
    if (!roomName) {
      alert('Please enter a room name to join.');
      return;
    }
    if (!this.userId) {
      alert('You must be logged in to join a room.');
      void this.router.navigate(['/login']);
      return;
    }

    this.roomSessionService.joinRoomByName(roomName, this.userId, this.userName).subscribe({
      next: (joinedRoom) => {
        this.joinRoomName = '';
        this.router.navigate(['/rooms', joinedRoom.roomId]);
      },
      error: (err) => {
        const message = err?.error?.error || err?.message || 'Unable to join room';
        alert(message);
      }
    });
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleString();
  }

  loadActiveRooms(): void {
    this.loading = true;
    this.error = '';
    this.roomSessionService.getMyActiveRoomsByRole(this.userId).subscribe({
      next: (roomsByRole) => {
        this.hostRooms = roomsByRole.hostRooms || [];
        this.participantRooms = roomsByRole.participantRooms || [];
        this.loading = false;
      },
      error: () => {
        // Fallback for older backend versions that don't provide /active/by-user.
        this.loadActiveRoomsFallback();
      }
    });
  }

  private loadActiveRoomsFallback(): void {
    this.roomSessionService.getActiveRooms().subscribe({
      next: (rooms) => {
        const participantLookups = rooms.map((room) =>
          this.roomSessionService.getParticipants(room.id).pipe(
            map((participants) => ({ room, participants })),
            catchError(() => of({ room, participants: [] as any[] }))
          )
        );

        if (participantLookups.length === 0) {
          this.hostRooms = [];
          this.participantRooms = [];
          this.loading = false;
          return;
        }

        forkJoin(participantLookups).subscribe({
          next: (results) => {
            const hostRooms: RoomSession[] = [];
            const participantRooms: RoomSession[] = [];

            results.forEach(({ room, participants }) => {
              const me = participants.find((p: any) => p.userId === this.userId && !p.leftAt);
              if (!me) return;
              if (me.role === 'HOST') {
                hostRooms.push(room);
              } else {
                participantRooms.push(room);
              }
            });

            this.hostRooms = hostRooms;
            this.participantRooms = participantRooms;
            this.loading = false;
          },
          error: () => {
            this.error = `Failed to connect to server. Make sure backend is running on ${environment.apiUrl}.`;
            this.loading = false;
          }
        });
      },
      error: () => {
        this.error = `Failed to connect to server. Make sure backend is running on ${environment.apiUrl}.`;
        this.loading = false;
      }
    });
  }
}