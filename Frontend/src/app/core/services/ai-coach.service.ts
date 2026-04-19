import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AiCoachApplyRequest,
  AiCoachPreviewRequest,
  AiCoachPreviewResponse
} from '../models/ai-coach.model';
import { Application } from '../models/application.model';

@Injectable({ providedIn: 'root' })
export class AiCoachService {
  private base = '/skillhub/api/ai/coach';

  constructor(private http: HttpClient) {}

  preview(request: AiCoachPreviewRequest): Observable<AiCoachPreviewResponse> {
    return this.http.post<AiCoachPreviewResponse>(`${this.base}/preview`, request);
  }

  apply(applicationId: number, applicantId: number, request: AiCoachApplyRequest): Observable<Application> {
    return this.http.post<Application>(`${this.base}/apply/${applicationId}/${applicantId}`, request);
  }
}
