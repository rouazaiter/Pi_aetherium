import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoomSessionService, RoomSession, Participant } from '../../../core/services/room-session/room-session.service';
import { WebSocketService } from '../../../core/services/room-session/websocket.service';
import { AgoraService } from '../../../core/services/room-session/agora.service';
import { RecordingService, RecordingState } from '../../../core/services/room-session/recording.service';
import { VideoPanelComponent } from '../video-panel/video-panel.component';
import { ChatPanelComponent } from '../chat-panel/chat-panel.component';
import { AppLayoutComponent } from '../app-layout/app-layout.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';
import { EditorComponent } from '../editor/editor.component';

@Component({
  selector: 'app-room-session',
  standalone: true,
  imports: [
    CommonModule,
    AppLayoutComponent,
    SidebarComponent,
    TopbarComponent,
    EditorComponent,
    VideoPanelComponent,
    ChatPanelComponent
  ],
  templateUrl: './room-session.component.html',
  styleUrls: ['./room-session.component.scss']
})
export class RoomSessionComponent implements OnInit, OnDestroy {
  roomId = 0;
  userId = 0;
  userName = '';
  room: RoomSession | null = null;
  participants: Participant[] = [];
  isHost = false;
  activeWorkspaceTab: 'code' | 'whiteboard' = 'code';
  recordingError = '';
  isRecording = false;
  recordingState: RecordingState = {
    isRecording: false,
    isPaused: false,
    duration: 0,
    recordingType: null
  };

  private destroy$ = new Subject<void>();
  private recordedBlob: Blob | null = null;

  constructor(
    private route: ActivatedRoute,
    private roomSessionService: RoomSessionService,
    private websocketService: WebSocketService,
    private agoraService: AgoraService,
    private recordingService: RecordingService
  ) {}

  ngOnInit(): void {
    this.roomId = +this.route.snapshot.paramMap.get('roomId')!;
    this.userId = +localStorage.getItem('userId')!;
    this.userName = localStorage.getItem('userName') || 'User';

    this.loadRoom();
    this.connectWebSocket();
    this.subscribeToRecordingState();
    this.subscribeToRoomEvents();
  }

  ngOnDestroy(): void {
    this.websocketService.disconnect();
    this.agoraService.leaveRoom();
    this.destroy$.next();
    this.destroy$.complete();
  }

  async toggleRecording(): Promise<void> {
    this.recordingError = '';
    if (this.isRecording) {
      return;
    }
    this.isRecording = true;
    try {
      if (this.recordingState.isRecording) {
        this.recordedBlob = await this.recordingService.stopRecording();
        if (!this.recordedBlob || this.recordedBlob.size === 0) {
          this.recordingError = 'Recording is empty. Please retry capture.';
          return;
        }
        await this.recordingService.uploadRecording(this.roomId, this.recordedBlob);
      } else {
        await this.recordingService.startScreenRecording();
      }
    } catch (error: any) {
      this.recordingError = error?.message || 'Recording failed. Please try again.';
    } finally {
      this.isRecording = false;
    }
  }

  endSession(): void {
    this.roomSessionService.endRoom(this.roomId, this.userId).subscribe();
  }

  leaveSession(): void {
    this.roomSessionService.leaveRoom(this.roomId, this.userId).subscribe();
  }

  formatDuration(seconds: number): string {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    if (hrs > 0) {
      return `${hrs}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  private loadRoom(): void {
    this.roomSessionService.getRoom(this.roomId).subscribe(room => {
      this.room = room;
      this.isHost = room.hostUserId === this.userId;
      document.title = `${room.name} - Room Session`;
    });

    this.roomSessionService.getParticipants(this.roomId).subscribe(participants => {
      this.participants = participants;
    });
  }

  private connectWebSocket(): void {
    this.websocketService.connect(this.roomId);
  }

  private subscribeToRecordingState(): void {
    this.recordingService.recordingState$
      .pipe(takeUntil(this.destroy$))
      .subscribe(state => {
        this.recordingState = state;
      });
  }

  private subscribeToRoomEvents(): void {
    this.websocketService.roomEvents$
      .pipe(takeUntil(this.destroy$))
      .subscribe(event => {
        if (event.type === 'ROOM_ENDED') {
          window.location.href = '/rooms';
        }
      });
  }
}