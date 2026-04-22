import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { RoomSessionService } from './room-session.service';

declare global {
  interface Window {
    AgoraRTC: any;
  }
}

export interface RemoteUser {
  uid: number | string;
  audioEnabled: boolean;
  videoEnabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AgoraService {
  private client: any = null;
  private localTracks: { videoTrack: any; audioTrack: any } = { videoTrack: null, audioTrack: null };
  private remoteUsers: Map<number | string, RemoteUser> = new Map();

  private isJoinedSubject = new BehaviorSubject<boolean>(false);
  private remoteUsersSubject = new BehaviorSubject<RemoteUser[]>([]);
  private errorSubject = new Subject<string>();

  isJoined$ = this.isJoinedSubject.asObservable();
  remoteUsers$ = this.remoteUsersSubject.asObservable();
  error$ = this.errorSubject.asObservable();

  constructor(
    private roomSessionService: RoomSessionService,
    private ngZone: NgZone
  ) {}

  async joinRoom(roomId: number, userId: number, containerId: string): Promise<void> {
    try {
      const tokenData = await this.roomSessionService.getAgoraToken(roomId, userId).toPromise();
      if (!tokenData) {
        throw new Error('Failed to get Agora token');
      }

      const { token, appId, channelName } = tokenData;

      if (!window.AgoraRTC) {
        console.error('Agora SDK not loaded');
        this.errorSubject.next('Agora SDK not loaded. Please include agora-rtc-sdk-ng in your project.');
        return;
      }

      this.client = window.AgoraRTC.createClient({
        mode: 'rtc',
        codec: 'vp8'
      });

      this.client.on('user-published', async (user: any, mediaType: string) => {
        await this.client.subscribe(user, mediaType);
        this.ngZone.run(() => {
          this.updateRemoteUser(user.uid, mediaType, true);
        });
      });

      this.client.on('user-unpublished', (user: any, mediaType: string) => {
        this.ngZone.run(() => {
          this.updateRemoteUser(user.uid, mediaType, false);
        });
      });

      this.client.on('user-left', (user: any) => {
        this.ngZone.run(() => {
          this.remoteUsers.delete(user.uid);
          this.emitRemoteUsers();
        });
      });

      await this.client.join(appId, channelName, token, userId);

      this.localTracks.videoTrack = await window.AgoraRTC.createMicrophoneVideoTrack();
      this.localTracks.audioTrack = await window.AgoraRTC.createMicrophoneAudioTrack();

      await this.client.publish([this.localTracks.videoTrack, this.localTracks.audioTrack]);

      this.isJoinedSubject.next(true);
    } catch (error: any) {
      console.error('Error joining room:', error);
      this.errorSubject.next(error.message || 'Failed to join video room');
    }
  }

  async leaveRoom(): Promise<void> {
    try {
      if (this.localTracks.videoTrack) {
        this.localTracks.videoTrack.close();
        this.localTracks.videoTrack = null;
      }
      if (this.localTracks.audioTrack) {
        this.localTracks.audioTrack.close();
        this.localTracks.audioTrack = null;
      }
      if (this.client) {
        await this.client.leave();
        this.client = null;
      }
      this.remoteUsers.clear();
      this.isJoinedSubject.next(false);
      this.emitRemoteUsers();
    } catch (error) {
      console.error('Error leaving room:', error);
    }
  }

  async toggleVideo(): Promise<boolean> {
    if (this.localTracks.videoTrack) {
      const enabled = await this.localTracks.videoTrack.setEnabled(!this.localTracks.videoTrack.enabled);
      return enabled;
    }
    return false;
  }

  async toggleAudio(): Promise<boolean> {
    if (this.localTracks.audioTrack) {
      const enabled = await this.localTracks.audioTrack.setEnabled(!this.localTracks.audioTrack.enabled);
      return enabled;
    }
    return false;
  }

  getLocalVideoTrack(): any {
    return this.localTracks.videoTrack;
  }

  getLocalAudioTrack(): any {
    return this.localTracks.audioTrack;
  }

  isJoined(): boolean {
    return this.isJoinedSubject.value;
  }

  private updateRemoteUser(uid: number | string, mediaType: string, enabled: boolean) {
    let user = this.remoteUsers.get(uid);
    if (!user) {
      user = { uid, audioEnabled: false, videoEnabled: false };
      this.remoteUsers.set(uid, user);
    }
    if (mediaType === 'video') {
      user.videoEnabled = enabled;
    } else if (mediaType === 'audio') {
      user.audioEnabled = enabled;
    }
    this.emitRemoteUsers();
  }

  private emitRemoteUsers() {
    this.remoteUsersSubject.next(Array.from(this.remoteUsers.values()));
  }
}