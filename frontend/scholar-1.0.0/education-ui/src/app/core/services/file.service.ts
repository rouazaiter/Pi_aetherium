import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FileService {

  private api = 'http://localhost:8081/api/files';

  constructor(private http: HttpClient) {}

  getFiles(userId: number) {
    return this.http.get<any[]>(`${this.api}?userId=${userId}`);
  }

  upload(file: File, userId: number) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId.toString());

    return this.http.post(this.api + '/upload', formData);
  }

  delete(id: number) {
    return this.http.delete(`${this.api}/${id}`);
  }

  rename(id: number, name: string) {
    return this.http.put(`${this.api}/${id}?name=${name}`, {});
  }

  download(id: number) {
  return this.http.get(`${this.api}/download/${id}`, {
    responseType: 'blob'
  });
}

getFileUrl(id: number) {
  return `${this.api}/download/${id}`;
}

summarize(fileId: number) {
  return this.http.post<any>(
    `${this.api}/summarize/${fileId}`,
    {}
  );
}

aiSummary(fileId: number) {
  return this.http.post<any>(
    `${this.api}/ai-summary/${fileId}`,
    {}
  );
}


startAiSummary(fileId: number) {
  return this.http.post<number>(
    `http://localhost:8081/api/files/ai-summary/${fileId}`,
    {}
  );
}

getAiSummaryStatus(jobId: number) {
  return this.http.get<any>(
    `http://localhost:8081/api/files/ai-summary/status/${jobId}`
  );
}


searchFiles(userId: number, keyword: string) {
  return this.http.get<any[]>(
    `http://localhost:8081/api/files/search?userId=${userId}&keyword=${keyword}`
  );
}


}