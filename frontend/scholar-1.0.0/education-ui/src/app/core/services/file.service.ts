import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TranscriptionResponse {
  status: 'PENDING' | 'DONE' | 'ERROR';
  result: string;
}

@Injectable({ providedIn: 'root' })
export class FileService {

  private api = 'http://localhost:8081/api/files';

  constructor(private http: HttpClient) {}

  getFiles(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}?userId=${userId}`);
  }

  upload(file: File, userId: number): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId.toString());

    return this.http.post(`${this.api}/upload`, formData);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.api}/${id}`);
  }

  rename(id: number, name: string): Observable<any> {
    return this.http.put(`${this.api}/${id}?name=${name}`, {});
  }

  download(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/download/${id}`, {
      responseType: 'blob'
    });
  }

  getFileUrl(id: number): string {
    return `${this.api}/download/${id}`;
  }

  // Use this URL for video preview / streaming (avoids Content-Disposition: attachment)
  getStreamUrl(id: number): string {
    return `${this.api}/stream/${id}`;
  }

  // URL of the auto-generated JPG thumbnail (extracted by FFmpeg at upload time)
  getThumbnailUrl(id: number): string {
    return `${this.api}/thumbnail/${id}`;
  }

  // Interroge le Tuteur IA (RAG) sur la vidéo
  chatWithVideo(fileId: number, question: string): Observable<{answer: string}> {
    return this.http.post<{answer: string}>(`${this.api}/chat/${fileId}`, { question });
  }

  getSummary(fileId: number): Observable<string> {
    return this.http.get(`${this.api}/summary/${fileId}`, { responseType: 'text' });
  }

  // Récupère le Bilan de Session structuré (JSON) généré par l'IA
  getReport(fileId: number): Observable<any> {
    return this.http.get<any>(`${this.api}/report/${fileId}`);
  }

  // Interroge TOUT le Drive (tous les bilans fusionnés)
  globalChat(userId: number, question: string): Observable<{answer: string}> {
    return this.http.post<{answer: string}>(`${this.api}/global-chat`, { userId, question });
  }

  searchFiles(userId: number, keyword: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.api}/search?userId=${userId}&keyword=${keyword}`
    );
  }

  // ===== TRANSCRIPTION =====
  startTranscription(fileId: number): Observable<number> {
    return this.http.post<number>(`${this.api}/transcribe/${fileId}`, {});
  }

  getTranscriptionStatus(jobId: number): Observable<TranscriptionResponse> {
    return this.http.get<TranscriptionResponse>(
      `${this.api}/transcribe/status/${jobId}`
    );
  }

  downloadTranscript(jobId: number): Observable<Blob> {
    return this.http.get(`${this.api}/transcript/${jobId}`, {
      responseType: 'blob'
    });
  }


getSummaryFile(path: string) {
  return this.http.get(
    `http://localhost:8081/api/files/summary-file`,
    {
      params: { path },
      responseType: 'text'
    }
  );
}

}