import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

export interface RemoteUser {
  uid: string;
  stream: MediaStream | null;
}

export interface WebRTCConfig {
  iceServers: RTCIceServer[];
}

@Injectable({
  providedIn: 'root'
})
export class WebRTCService {
  private localStream: MediaStream | null = null;
  private peerConnections: Map<string, RTCPeerConnection> = new Map();
  private remoteStreams: Map<string, MediaStream> = new Map();

  private localStreamSubject = new BehaviorSubject<MediaStream | null>(null);
  private remoteStreamsSubject = new BehaviorSubject<RemoteUser[]>([]);
  private errorSubject = new Subject<string>();

  private config: WebRTCConfig = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' }
    ]
  };

  localStream$ = this.localStreamSubject.asObservable();
  remoteStreams$ = this.remoteStreamsSubject.asObservable();
  error$ = this.errorSubject.asObservable();

  constructor(private ngZone: NgZone) {}

  async startLocalStream(video: boolean = true, audio: boolean = true): Promise<MediaStream> {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        video: video ? {
          width: { ideal: 1280 },
          height: { ideal: 720 }
        } : false,
        audio: audio ? {
          echoCancellation: true,
          noiseSuppression: true
        } : false
      });

      this.localStreamSubject.next(this.localStream);
      return this.localStream;
    } catch (error: any) {
      console.error('Error starting local stream:', error);
      this.errorSubject.next('Could not access camera/microphone: ' + error.message);
      throw error;
    }
  }

  async stopLocalStream(): Promise<void> {
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
      this.localStreamSubject.next(null);
    }
  }

  async createPeerConnection(peerId: string): Promise<RTCPeerConnection> {
    const pc = new RTCPeerConnection(this.config);

    pc.onicecandidate = (event) => {
      if (event.candidate) {
        this.onIceCandidate(peerId, event.candidate);
      }
    };

    pc.ontrack = (event) => {
      this.ngZone.run(() => {
        this.remoteStreams.set(peerId, event.streams[0]);
        this.emitRemoteStreams();
      });
    };

    pc.oniceconnectionstatechange = () => {
      console.log(`ICE connection state for ${peerId}:`, pc.iceConnectionState);
    };

    if (this.localStream) {
      this.localStream.getTracks().forEach(track => {
        pc.addTrack(track, this.localStream!);
      });
    }

    this.peerConnections.set(peerId, pc);
    return pc;
  }

  async createOffer(peerId: string): Promise<RTCSessionDescriptionInit> {
    const pc = this.peerConnections.get(peerId);
    if (!pc) throw new Error('No peer connection for ' + peerId);

    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    return offer;
  }

  async handleOffer(peerId: string, offer: RTCSessionDescriptionInit): Promise<RTCSessionDescriptionInit> {
    let pc = this.peerConnections.get(peerId);
    if (!pc) {
      pc = await this.createPeerConnection(peerId);
    }

    await pc.setRemoteDescription(offer);
    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
    return answer;
  }

  async handleAnswer(peerId: string, answer: RTCSessionDescriptionInit): Promise<void> {
    const pc = this.peerConnections.get(peerId);
    if (!pc) throw new Error('No peer connection for ' + peerId);

    await pc.setRemoteDescription(answer);
  }

  async addIceCandidate(peerId: string, candidate: RTCIceCandidateInit): Promise<void> {
    const pc = this.peerConnections.get(peerId);
    if (!pc) return;

    await pc.addIceCandidate(candidate);
  }

  closePeerConnection(peerId: string): void {
    const pc = this.peerConnections.get(peerId);
    if (pc) {
      pc.close();
      this.peerConnections.delete(peerId);
    }

    const stream = this.remoteStreams.get(peerId);
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      this.remoteStreams.delete(peerId);
      this.emitRemoteStreams();
    }
  }

  closeAllConnections(): void {
    this.peerConnections.forEach((pc, peerId) => {
      pc.close();
    });
    this.peerConnections.clear();

    this.remoteStreams.forEach((stream, peerId) => {
      stream.getTracks().forEach(track => track.stop());
    });
    this.remoteStreams.clear();
    this.emitRemoteStreams();

    this.stopLocalStream();
  }

  async toggleVideo(): Promise<boolean> {
    if (this.localStream) {
      const videoTrack = this.localStream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled;
        return videoTrack.enabled;
      }
    }
    return false;
  }

  async toggleAudio(): Promise<boolean> {
    if (this.localStream) {
      const audioTrack = this.localStream.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        return audioTrack.enabled;
      }
    }
    return false;
  }

  getLocalStream(): MediaStream | null {
    return this.localStream;
  }

  private async onIceCandidate(peerId: string, candidate: RTCIceCandidate): Promise<void> {
    console.log('New ICE candidate for', peerId, candidate);
  }

  private emitRemoteStreams(): void {
    const users: RemoteUser[] = Array.from(this.remoteStreams.entries()).map(([uid, stream]) => ({
      uid,
      stream
    }));
    this.remoteStreamsSubject.next(users);
  }
}