import { DatePipe } from '@angular/common';
import { Component, OnDestroy, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import type { FriendRequestResponse, FriendResponse, FriendSearchResponse } from '../../core/models/api.models';
import { FriendService } from '../../core/services/friend.service';
import { messageFromHttpError } from '../../core/util/http-error';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

@Component({
  selector: 'app-friends',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './friends.component.html',
  styleUrl: './friends.component.scss',
})
export class FriendsComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(FriendService);
  private readonly destroy$ = new Subject<void>();
  private refreshTimer: ReturnType<typeof setInterval> | null = null;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  readonly searchForm = this.fb.nonNullable.group({
    query: ['', [Validators.required, Validators.minLength(2)]],
  });

  activeView: 'discover' | 'requests' | 'friends' = 'discover';
  relationFilter: 'ALL' | 'NONE' | 'REQUEST_SENT' | 'REQUEST_RECEIVED' | 'FRIEND' = 'ALL';

  list: FriendResponse[] = [];
  results: FriendSearchResponse[] = [];
  incomingRequests: FriendRequestResponse[] = [];

  loadError = '';
  searchError = '';
  requestError = '';
  successMessage = '';
  popupVisible = true;
  loading = false;
  searching = false;
  loadingIncoming = false;
  requestingId: number | null = null;
  acceptingId: number | null = null;
  decliningId: number | null = null;

  ngOnInit(): void {
    this.refresh();
    this.loadIncomingRequests();

    this.searchForm.controls.query.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((query) => {
        const trimmed = query.trim();
        this.searchError = '';
        if (trimmed.length < 2) {
          this.results = [];
          return;
        }
        this.searchUsers(trimmed);
      });

    this.refreshTimer = setInterval(() => this.loadIncomingRequests(false), 15000);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
      this.toastTimer = null;
    }
  }

  refresh(): void {
    this.loading = true;
    this.loadError = '';
    this.api.list().subscribe({
      next: (rows) => {
        this.loading = false;
        this.list = rows;
      },
      error: (err) => {
        this.loading = false;
        this.loadError = messageFromHttpError(err, 'Impossible de charger la liste d’amis.');
      },
    });
  }

  manualSearch(): void {
    this.searchError = '';
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }
    this.searchUsers(this.searchForm.getRawValue().query.trim());
  }

  private searchUsers(query: string): void {
    this.searching = true;
    this.api.search(query).subscribe({
      next: (rows) => {
        this.searching = false;
        this.results = rows;
      },
      error: (err) => {
        this.searching = false;
        this.searchError = messageFromHttpError(err, 'Recherche impossible.');
      },
    });
  }

  sendRequest(userId: number): void {
    this.requestError = '';
    this.requestingId = userId;
    this.api.sendRequest(userId).subscribe({
      next: () => {
        this.requestingId = null;
        this.results = this.results.map((r) => (r.id === userId ? { ...r, relation: 'REQUEST_SENT' } : r));
        this.showToast('Invitation envoyee avec succes.');
      },
      error: (err) => {
        this.requestingId = null;
        this.requestError = messageFromHttpError(err, "Envoi de la demande impossible.");
      },
    });
  }

  loadIncomingRequests(updatePopup = true): void {
    this.loadingIncoming = true;
    this.api.listIncomingRequests().subscribe({
      next: (rows) => {
        this.loadingIncoming = false;
        this.incomingRequests = rows;
        if (updatePopup) {
          this.popupVisible = true;
        }
      },
      error: (err) => {
        this.loadingIncoming = false;
        this.loadError = messageFromHttpError(err, 'Impossible de charger les demandes recues.');
      },
    });
  }

  accept(requestId: number): void {
    this.requestError = '';
    this.acceptingId = requestId;
    this.api.acceptRequest(requestId).subscribe({
      next: () => {
        this.acceptingId = null;
        this.incomingRequests = this.incomingRequests.filter((r) => r.id !== requestId);
        this.popupVisible = this.incomingRequests.length > 0;
        this.refresh();
        this.showToast('Invitation acceptee. Vous etes maintenant amis.');
        if (this.results.length > 0) {
          this.manualSearch();
        }
      },
      error: (err) => {
        this.acceptingId = null;
        this.requestError = messageFromHttpError(err, "Acceptation de la demande impossible.");
      },
    });
  }

  decline(requestId: number): void {
    this.requestError = '';
    this.decliningId = requestId;
    this.api.declineRequest(requestId).subscribe({
      next: () => {
        this.decliningId = null;
        this.incomingRequests = this.incomingRequests.filter((r) => r.id !== requestId);
        this.popupVisible = this.incomingRequests.length > 0;
        this.showToast('Invitation refusee.');
        if (this.results.length > 0) {
          this.manualSearch();
        }
      },
      error: (err) => {
        this.decliningId = null;
        this.requestError = messageFromHttpError(err, 'Refus de la demande impossible.');
      },
    });
  }

  closePopup(): void {
    this.popupVisible = false;
  }

  remove(id: number): void {
    this.api.remove(id).subscribe({
      next: () => {
        this.refresh();
        this.showToast('Ami retire de votre liste.');
      },
      error: (err) => {
        this.loadError = messageFromHttpError(err, 'Suppression impossible.');
      },
    });
  }

  fullName(user: { firstName: string | null; lastName: string | null }): string {
    const full = `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim();
    return full || 'Nom non renseigne';
  }

  initials(user: { username: string; firstName: string | null; lastName: string | null }): string {
    const source = `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.trim();
    return (source || user.username.slice(0, 2)).toUpperCase();
  }

  relationLabel(relation: FriendSearchResponse['relation']): string {
    switch (relation) {
      case 'FRIEND':
        return 'Deja ami';
      case 'REQUEST_SENT':
        return 'Demande envoyee';
      case 'REQUEST_RECEIVED':
        return 'Invitation recue';
      default:
        return 'Disponible';
    }
  }

  setActiveView(view: 'discover' | 'requests' | 'friends'): void {
    this.activeView = view;
  }

  setRelationFilter(filter: 'ALL' | 'NONE' | 'REQUEST_SENT' | 'REQUEST_RECEIVED' | 'FRIEND'): void {
    this.relationFilter = filter;
  }

  get filteredResults(): FriendSearchResponse[] {
    if (this.relationFilter === 'ALL') {
      return this.results;
    }
    return this.results.filter((r) => r.relation === this.relationFilter);
  }

  get pendingRequestsCount(): number {
    return this.incomingRequests.length;
  }

  get popupProfiles(): Array<{ username: string; firstName: string | null; lastName: string | null }> {
    const source = [
      ...this.incomingRequests.map((r) => r.sender),
      ...this.list,
      ...this.results,
    ];
    const unique = new Map<string, { username: string; firstName: string | null; lastName: string | null }>();
    for (const user of source) {
      if (!unique.has(user.username)) {
        unique.set(user.username, {
          username: user.username,
          firstName: user.firstName ?? null,
          lastName: user.lastName ?? null,
        });
      }
      if (unique.size >= 6) {
        break;
      }
    }
    return Array.from(unique.values());
  }

  countByRelation(relation: FriendSearchResponse['relation']): number {
    return this.results.filter((r) => r.relation === relation).length;
  }

  acceptFromSearch(userId: number): void {
    const request = this.incomingRequests.find((r) => r.sender.id === userId);
    if (!request) {
      this.requestError = "Demande introuvable pour cet utilisateur.";
      return;
    }
    this.accept(request.id);
  }

  declineFromSearch(userId: number): void {
    const request = this.incomingRequests.find((r) => r.sender.id === userId);
    if (!request) {
      this.requestError = "Demande introuvable pour cet utilisateur.";
      return;
    }
    this.decline(request.id);
  }

  private showToast(message: string): void {
    this.successMessage = message;
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
    this.toastTimer = setTimeout(() => {
      this.successMessage = '';
      this.toastTimer = null;
    }, 2600);
  }
}
