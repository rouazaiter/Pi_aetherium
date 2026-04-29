import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { RecordingUploadService } from './recording-upload.service';

export interface RecordingState {
  isRecording: boolean;
  isPaused: boolean;
  duration: number;
  recordingType: 'VIDEO' | 'WHITEBOARD' | 'IDE' | null;
}

@Injectable({
  providedIn: 'root'
})
export class RecordingService {
  private mediaRecorder: MediaRecorder | null = null;
  private recordedChunks: Blob[] = [];
  private displayStream: MediaStream | null = null;
  private microphoneStream: MediaStream | null = null;
  private combinedStream: MediaStream | null = null;
  private durationInterval: any = null;
  private errorSubject = new BehaviorSubject<string | null>(null);
  private recordingCount = 0;
  private currentSessionId: number | null = null;

  private recordingStateSubject = new BehaviorSubject<RecordingState>({
    isRecording: false,
    isPaused: false,
    duration: 0,
    recordingType: null
  });

  recordingState$ = this.recordingStateSubject.asObservable();
  error$ = this.errorSubject.asObservable();

  constructor(private recordingUploadService: RecordingUploadService) {}

  async startScreenRecording(): Promise<void> {
    if (this.mediaRecorder?.state === 'recording') {
      console.log('Already recording');
      return;
    }

    try {
      this.errorSubject.next(null);
      this.recordedChunks = [];

      console.log('Starting screen capture...');
      this.displayStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: false
      });
      console.log('Display stream acquired:', this.displayStream.id);

      const videoTrack = this.displayStream.getVideoTracks()[0];
      console.log('Video track:', videoTrack?.id, videoTrack?.readyState);
      if (!videoTrack) {
        throw new DOMException('No video track available', 'NotReadableError');
      }

      try {
        console.log('Getting microphone...');
        this.microphoneStream = await navigator.mediaDevices.getUserMedia({ audio: true });
        console.log('Microphone stream acquired');
      } catch {
        console.log('Microphone not available');
        this.microphoneStream = null;
      }

      const tracks: MediaStreamTrack[] = [
        ...this.displayStream.getVideoTracks(),
        ...(this.microphoneStream ? this.microphoneStream.getAudioTracks() : [])
      ];

      console.log('Combined tracks:', tracks.length);
      this.combinedStream = new MediaStream(tracks);

      if (this.combinedStream.getVideoTracks().length === 0) {
        throw new DOMException('No video track in combined stream', 'NotReadableError');
      }

      const mimeType = this.getSupportedMimeType();
      console.log('Using mimeType:', mimeType);

      const options: MediaRecorderOptions = {
        mimeType
      };

      console.log('Creating MediaRecorder...');
      this.mediaRecorder = new MediaRecorder(this.combinedStream, options);

      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.recordedChunks.push(event.data);
        }
      };

      this.mediaRecorder.onerror = (event: any) => {
        console.error('MediaRecorder error:', event);
      };

      this.mediaRecorder.onstop = () => {
        console.log('Recording stopped');
      };

      console.log('Starting MediaRecorder...');
      try {
        this.mediaRecorder.start(1000);
      } catch (startError: any) {
        console.error('MediaRecorder.start error:', startError);
        throw startError;
      }

      console.log('MediaRecorder state:', this.mediaRecorder.state);
      if (this.mediaRecorder.state !== 'recording') {
        throw new DOMException('Failed to start recording', 'AbortError');
      }

      this.startDurationTimer();

      this.updateState({
        isRecording: true,
        isPaused: false,
        duration: 0,
        recordingType: 'VIDEO'
      });

      if (videoTrack) {
        videoTrack.onended = () => {
          if (this.mediaRecorder?.state === 'recording') {
            void this.stopRecording().catch(() => undefined);
          }
        };
      }
    } catch (error: any) {
      this.cleanupStream();
      this.stopDurationTimer();
      this.updateState({
        isRecording: false,
        isPaused: false,
        duration: 0,
        recordingType: null
      });

      const message = this.mapCaptureError(error);
      if (message) {
        this.errorSubject.next(message);
        throw new Error(message);
      }
    }
  }

  async stopRecording(): Promise<Blob> {
    return new Promise((resolve) => {
      if (!this.mediaRecorder || this.mediaRecorder.state === 'inactive') {
        resolve(new Blob([], { type: 'video/webm' }));
        return;
      }

      this.mediaRecorder.onstop = () => {
        const blob = new Blob(this.recordedChunks, {
          type: this.getSupportedMimeType()
        });

        this.stopDurationTimer();
        this.cleanupStream();

        this.updateState({
          isRecording: false,
          isPaused: false,
          duration: 0,
          recordingType: null
        });

        resolve(blob);
      };

      this.mediaRecorder.stop();
    });
  }

  pauseRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.pause();
      this.stopDurationTimer();
      this.updateState({ isPaused: true });
    }
  }

  resumeRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state === 'paused') {
      this.mediaRecorder.resume();
      this.startDurationTimer();
      this.updateState({ isPaused: false });
    }
  }

  async uploadRecording(sessionId: number, blob: Blob): Promise<void> {
    try {
      this.errorSubject.next(null);

      if (this.currentSessionId !== sessionId) {
        console.log('New session detected:', sessionId, 'previous:', this.currentSessionId);
        this.currentSessionId = sessionId;
        this.recordingCount = 0;
      }
      this.recordingCount++;

      console.log('Uploading recording:', sessionId, this.recordingCount);
      const fileName = `${sessionId}_${this.recordingCount}.webm`;
      await this.recordingUploadService.uploadScreenRecording(sessionId, blob, fileName);
    } catch {
      const message = 'Upload failed. Please verify backend availability and file size limits.';
      this.errorSubject.next(message);
      throw new Error(message);
    }
  }

  getState(): RecordingState {
    return this.recordingStateSubject.value;
  }

  private getSupportedMimeType(): string {
    const mimeTypes = [
      'video/webm;codecs=vp9',
      'video/webm;codecs=vp8',
      'video/webm'
    ];

    for (const mimeType of mimeTypes) {
      if (MediaRecorder.isTypeSupported(mimeType)) {
        return mimeType;
      }
    }
    return 'video/webm';
  }

  private startDurationTimer(): void {
    this.durationInterval = setInterval(() => {
      const currentState = this.recordingStateSubject.value;
      this.updateState({ duration: currentState.duration + 1 });
    }, 1000);
  }

  private stopDurationTimer(): void {
    if (this.durationInterval) {
      clearInterval(this.durationInterval);
      this.durationInterval = null;
    }
  }

  private cleanupStream(): void {
    [this.combinedStream, this.displayStream, this.microphoneStream]
      .filter((s): s is MediaStream => !!s)
      .forEach((stream) => stream.getTracks().forEach((track) => track.stop()));

    this.combinedStream = null;
    this.displayStream = null;
    this.microphoneStream = null;
    this.mediaRecorder = null;
  }

  private mapCaptureError(error: any): string {
    const name = error?.name as string | undefined;
    console.error('Screen recording error:', error);
    if (name === 'AbortError') {
      const message = error?.message || '';
      if (message.includes('Timeout')) {
        return 'Timeout starting video source. Please try again.';
      }
      return '';
    }
    if (name === 'NotAllowedError') {
      return 'Screen capture permission denied.';
    }
    if (name === 'NotFoundError') {
      return 'No screen or tab source was available to record.';
    }
    if (name === 'NotReadableError') {
      return 'Display source is already in use.';
    }
    if (name === 'OverconstrainedError') {
      return 'Display source does not support the requested options.';
    }
    return 'Failed to start screen recording.';
  }

  private updateState(partialState: Partial<RecordingState>): void {
    const currentState = this.recordingStateSubject.value;
    this.recordingStateSubject.next({ ...currentState, ...partialState });
  }
}