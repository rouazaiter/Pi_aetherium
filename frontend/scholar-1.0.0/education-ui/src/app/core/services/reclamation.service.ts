import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import type {
  AdminReclamationUpdateRequest,
  CreateReclamationRequest,
  ReclamationResponse,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ReclamationService {
  private readonly http = inject(HttpClient);
  /** Préfixe `/api/social/reclamations` (même arbre que le Social Hub). Le backend accepte aussi `/api/reclamations`. */
  private readonly base = `${environment.apiUrl}/api/social/reclamations`;
  private readonly adminBase = `${environment.apiUrl}/api/admin/reclamations`;

  create(body: CreateReclamationRequest) {
    return this.http.post<ReclamationResponse>(this.base, body);
  }

  mine() {
    return this.http.get<ReclamationResponse[]>(this.base);
  }

  getOne(id: number) {
    return this.http.get<ReclamationResponse>(`${this.base}/${id}`);
  }

  adminList() {
    return this.http.get<ReclamationResponse[]>(this.adminBase);
  }

  adminUpdate(id: number, body: AdminReclamationUpdateRequest) {
    return this.http.patch<ReclamationResponse>(`${this.adminBase}/${id}`, body);
  }
}
