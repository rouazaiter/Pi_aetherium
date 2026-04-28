import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export type MeetingStatus = 'PENDING' | 'CONFIRMED' | 'DECLINED';
export type MeetingSource = 'SLOTS' | 'CALENDLY';

export interface RequestSchedulingConfig {
  requestId: number;
  calendlyLink?: string;
  durationMinutes?: number;
  availableSlots: string[];
  updatedAt: string;
}

export interface CalendlyEventDetails {
  uri: string;
  name: string;
  startTime: string;
  endTime: string;
  status: string;
  location?: string;
  inviteeUrl?: string;
}

export interface MeetingReservation {
  requestId: number;
  applicationId: number;
  applicantId: number;
  applicantUsername: string;
  slot: string;
  source: MeetingSource;
  calendlyEventUrl?: string;
  candidateCalendlyUrl?: string;
  calendlyEventDetails?: CalendlyEventDetails;
  status: MeetingStatus;
  createdAt: string;
  confirmedAt?: string;
}

interface MeetingConfigApiResponse {
  serviceRequestId: number;
  calendlyLink?: string;
  durationMinutes?: number;
  availableSlots: string[];
  updatedAt: string;
}

interface MeetingReservationApiResponse {
  id: number;
  applicationId: number;
  serviceRequestId: number;
  applicantId: number;
  applicantUsername: string;
  source: MeetingSource;
  slot: string;
  calendlyEventUrl?: string;
  candidateCalendlyUrl?: string;
  status: MeetingStatus;
  createdAt: string;
  confirmedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class MeetingSchedulerService {
  private base = '/skillhub/api/meetings';

  constructor(private http: HttpClient) {}

  getConfig(requestId: number): Observable<RequestSchedulingConfig> {
    return this.http
      .get<MeetingConfigApiResponse>(`${this.base}/config/${requestId}`)
      .pipe(map(res => ({
        requestId: res.serviceRequestId,
        calendlyLink: res.calendlyLink,
        durationMinutes: res.durationMinutes,
        availableSlots: res.availableSlots ?? [],
        updatedAt: res.updatedAt
      })));
  }
//save calendly 
  saveConfig(
    requestId: number,
    requesterId: number,
    calendlyLink: string,
    durationMinutes: number,
    availableSlots: string[]
  ): Observable<RequestSchedulingConfig> {
    return this.http
      .put<MeetingConfigApiResponse>(`${this.base}/config/${requestId}/${requesterId}`, {
        calendlyLink: (calendlyLink ?? '').trim() || null,
        durationMinutes,
        availableSlots
      })
      .pipe(map(res => ({
        requestId: res.serviceRequestId,
        calendlyLink: res.calendlyLink,
        durationMinutes: res.durationMinutes,
        availableSlots: res.availableSlots ?? [],
        updatedAt: res.updatedAt
      })));
  }

  reserveSlot(
    applicationId: number,
    applicantId: number,
    source: MeetingSource,
    slot: string,
    candidateCalendlyUrl?: string
  ): Observable<MeetingReservation> {
    return this.http
      .post<MeetingReservationApiResponse>(`${this.base}/reserve/${applicationId}/${applicantId}`, {
        source,
        slot,
        candidateCalendlyUrl: candidateCalendlyUrl || null
      })
      .pipe(map(this.mapReservation));
  }

  getBookingByApplication(applicationId: number, requesterId: number): Observable<MeetingReservation> {
    return this.http
      .get<MeetingReservationApiResponse>(`${this.base}/by-application/${applicationId}/${requesterId}`)
      .pipe(map(this.mapReservation));
  }

  getCalendlyEventDetails(eventUrl: string): Observable<CalendlyEventDetails> {
    return this.http.get<CalendlyEventDetails>(`${this.base}/calendly-event`, {
      params: { eventUrl }
    });
  }

  updateBookingStatus(applicationId: number, requesterId: number, status: MeetingStatus): Observable<MeetingReservation> {
    return this.http
      .patch<MeetingReservationApiResponse>(`${this.base}/status/${applicationId}/${requesterId}/${status}`, {})
      .pipe(map(this.mapReservation));
  }

  private normalizeSlots(slots: string[]): string[] {
    const unique = new Set<string>();

    for (const rawSlot of slots ?? []) {
      const slot = rawSlot.trim();
      if (!slot) {
        continue;
      }
      unique.add(slot);
    }

    return Array.from(unique).sort((a, b) => a.localeCompare(b));
  }

  private mapReservation(response: MeetingReservationApiResponse): MeetingReservation {
    return {
      requestId: response.serviceRequestId,
      applicationId: response.applicationId,
      applicantId: response.applicantId,
      applicantUsername: response.applicantUsername,
      slot: response.slot,
      source: response.source,
      calendlyEventUrl: response.calendlyEventUrl,
      candidateCalendlyUrl: response.candidateCalendlyUrl,
      status: response.status,
      createdAt: response.createdAt,
      confirmedAt: response.confirmedAt
    };
  }
}
