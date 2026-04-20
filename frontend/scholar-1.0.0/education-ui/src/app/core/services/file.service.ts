import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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
}