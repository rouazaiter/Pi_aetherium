import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface RecordingUploadResponse {
  sessionId: number;
  fileName: string;
  path: string;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class RecordingUploadService {
  private readonly apiUrl = '/api/recordings';

  constructor(private http: HttpClient) {}

  async uploadScreenRecording(sessionId: number, videoBlob: Blob, fileName: string): Promise<RecordingUploadResponse> {
    console.log('Upload service - fileName:', fileName, 'size:', videoBlob.size);
    const formData = new FormData();
    const file = new File([videoBlob], fileName, { type: 'video/webm' });
    formData.append('video', file);
    formData.append('sessionId', String(sessionId));
    formData.append('fileName', fileName);

    return firstValueFrom(
      this.http.post<RecordingUploadResponse>(`${this.apiUrl}/upload`, formData)
    );
  }
}

