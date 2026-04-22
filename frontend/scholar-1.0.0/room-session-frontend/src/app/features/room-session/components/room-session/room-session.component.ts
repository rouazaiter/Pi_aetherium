import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoomSessionService, RoomSession, Participant } from '../../services/room-session.service';
import { WebSocketService } from '../../services/websocket.service';
import { AgoraService } from '../../services/agora.service';
import { RecordingService, RecordingState } from '../../services/recording.service';
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
  template: `
    <app-app-layout>
      <app-topbar
        [roomName]="room?.name || ('Room #' + roomId)"
        [isRecording]="recordingState.isRecording"
        [recordingDuration]="recordingState.duration"
        (recordingToggle)="toggleRecording()">
      </app-topbar>

      <app-sidebar [activeSection]="activeWorkspaceTab" (activeSectionChange)="activeWorkspaceTab = $event"></app-sidebar>

      <app-editor
        layout-main
        [roomId]="roomId"
        [activeTab]="activeWorkspaceTab"
        (activeTabChange)="activeWorkspaceTab = $event">
      </app-editor>

      <aside layout-right class="w-[320px] bg-white border-l border-slate-200 flex flex-col h-full shrink-0">
        <div class="h-1/2 border-b border-slate-200 flex flex-col overflow-hidden">
          <div class="p-3 border-b border-slate-100 flex items-center justify-between">
            <span class="font-label-md text-slate-900">Live Session ({{ participants.length || 1 }})</span>
            <div class="flex gap-1 items-center">
              <button *ngIf="isHost" type="button" class="p-1 hover:bg-slate-100 rounded" (click)="endSession()" title="End session">
                <span class="material-symbols-outlined text-[18px] text-slate-600">call_end</span>
              </button>
              <button *ngIf="!isHost" type="button" class="p-1 hover:bg-slate-100 rounded" (click)="leaveSession()" title="Leave session">
                <span class="material-symbols-outlined text-[18px] text-slate-600">logout</span>
              </button>
            </div>
          </div>
          <div *ngIf="recordingError" class="px-3 py-2 text-[11px] text-red-600 bg-red-50 border-b border-red-100">
            {{ recordingError }}
          </div>
          <div class="flex-1 min-h-0">
            <app-video-panel [roomId]="roomId" [userId]="userId" [isHost]="isHost" [userName]="userName"></app-video-panel>
          </div>
        </div>

        <div class="flex-1 flex flex-col min-h-0">
          <div class="p-3 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined text-[20px] text-[#464EB8]" style="font-variation-settings: 'FILL' 1;">forum</span>
              <span class="font-label-md text-slate-900">Team Chat</span>
            </div>
            <span class="bg-primary-container text-white text-[10px] px-1.5 rounded-full font-bold">{{ participants.length + 1 }}</span>
          </div>
          <div class="flex-1 min-h-0">
            <app-chat-panel [roomId]="roomId" [userId]="userId" [userName]="userName"></app-chat-panel>
          </div>
        </div>
      </aside>
    </app-app-layout>
  `
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