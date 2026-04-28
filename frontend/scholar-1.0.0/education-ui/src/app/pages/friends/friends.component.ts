import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Component, ElementRef, HostListener, OnDestroy, ViewChild, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { resolvePresetProfilePicture } from '../../core/data/preset-avatars';
import type {
  ConversationResponse,
  DirectMessageResponse,
  FriendProfileResponse,
  FriendRequestResponse,
  FriendResponse,
  FriendSearchResponse,
  ProfileResponse,
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { FriendService } from '../../core/services/friend.service';
import { MessageService } from '../../core/services/message.service';
import { ProfileService } from '../../core/services/profile.service';
import { messageFromHttpError } from '../../core/util/http-error';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

interface GiphyImageSet {
  original?: { url?: string; webp?: string };
  fixed_height_small?: { url?: string };
  fixed_height?: { url?: string };
}

interface GiphyItem {
  id?: string | number;
  images?: GiphyImageSet;
}

interface GiphySearchResponse {
  data?: GiphyItem[];
}

interface GifPickerResultItem {
  id: string;
  url: string;
  previewUrl: string;
}

const FALLBACK_GIFS: GifPickerResultItem[] = [
  {
    id: 'fallback-1',
    url: 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa2x0N2x4aG5xczM0emRnbWhvb2YwZ3N5eDE0a2lnYjM2dTE5a3QxYiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/l0HlBO7eyXzSZkJri/giphy.gif',
    previewUrl: 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExa2x0N2x4aG5xczM0emRnbWhvb2YwZ3N5eDE0a2lnYjM2dTE5a3QxYiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/l0HlBO7eyXzSZkJri/200w.gif',
  },
  {
    id: 'fallback-2',
    url: 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExdXh5eDN3N25zb2IzM3M5bHF1eDZmN2M5MndxN2owN3VwZjB4dHE2ZSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/3o7aD2saalBwwftBIY/giphy.gif',
    previewUrl: 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExdXh5eDN3N25zb2IzM3M5bHF1eDZmN2M5MndxN2owN3VwZjB4dHE2ZSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/3o7aD2saalBwwftBIY/200w.gif',
  },
  {
    id: 'fallback-3',
    url: 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExN3hoNWQ1N2x2cjVvMzd5NTR2NTF2bWJqajV5dWJ6bmZ4eWg2a2N5ZCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/xUPGcguWZHRC2HyBRS/giphy.gif',
    previewUrl: 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExN3hoNWQ1N2x2cjVvMzd5NTR2NTF2bWJqajV5dWJ6bmZ4eWg2a2N5ZCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/xUPGcguWZHRC2HyBRS/200w.gif',
  },
];

@Component({
  selector: 'app-friends',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, FormsModule],
  templateUrl: './friends.component.html',
  styleUrl: './friends.component.scss',
})
export class FriendsComponent implements OnInit, OnDestroy {
  private static readonly MUTED_USERS_STORAGE_KEY = 'friends.mutedUserIds';
  private static readonly GIF_MARKER = '[[GIF]]';
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(FriendService);
  private readonly messaging = inject(MessageService);
  private readonly auth = inject(AuthService);
  private readonly profileApi = inject(ProfileService);
  private readonly http = inject(HttpClient);
  private readonly destroy$ = new Subject<void>();
  private refreshTimer: ReturnType<typeof setInterval> | null = null;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;
  private liveSource: EventSource | null = null;

  readonly searchForm = this.fb.nonNullable.group({
    query: ['', [Validators.required, Validators.minLength(2)]],
  });

  currentTopTab: 'network' | 'messages' | 'invitations' = 'network';
  activeView: 'discover' | 'requests' | 'friends' = 'discover';
  relationFilter: 'ALL' | 'NONE' | 'REQUEST_SENT' | 'REQUEST_RECEIVED' | 'FRIEND' = 'ALL';
  selectedNetworkSection: 'all' | 'online' = 'all';

  list: FriendResponse[] = [];
  results: FriendSearchResponse[] = [];
  incomingRequests: FriendRequestResponse[] = [];
  conversations: ConversationResponse[] = [];
  currentMessages: DirectMessageResponse[] = [];
  myProfile: ProfileResponse | null = null;

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
  selectedConversationId: number | null = null;
  loadingConversations = false;
  loadingMessages = false;
  sendingMessage = false;
  summarizingConversation = false;
  conversationSummary = '';
  conversationSummaryError = '';
  summaryConversationId: number | null = null;
  messageDraft = '';
  replyingToMessage: DirectMessageResponse | null = null;
  deleteMenuMessageId: number | null = null;
  recordingVoice = false;
  private mediaRecorder: MediaRecorder | null = null;
  private recordedChunks: Blob[] = [];
  gifPickerOpen = false;
  gifSearchQuery = '';
  gifLoading = false;
  gifError = '';
  gifInfo = '';
  gifResults: GifPickerResultItem[] = [];
  selectedFriendProfile: FriendProfileResponse | null = null;
  loadingFriendProfile = false;
  profileLoadError = '';
  mutedUserIds = new Set<number>();
  @ViewChild('composerInput') composerInput?: ElementRef<HTMLInputElement>;

  @HostListener('document:keydown', ['$event'])
  onDocumentKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && this.gifPickerOpen) {
      event.preventDefault();
      this.closeGifPicker();
    }
  }

  ngOnInit(): void {
    this.hydrateMutedUsers();
    this.loadCurrentProfileName();
    this.refresh();
    this.loadIncomingRequests();
    this.loadSuggestedUsers();
    this.initLiveMessaging();

    this.searchForm.controls.query.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((query) => {
        const trimmed = query.trim();
        this.searchError = '';
        if (trimmed.length < 2) {
          this.loadSuggestedUsers();
          return;
        }
        this.searchUsers(trimmed);
      });

    this.refreshTimer = setInterval(() => {
      this.loadIncomingRequests(false);
      // Avoid DB overload: when SSE live stream is connected, don't also poll messages every 4s.
      if (this.currentTopTab === 'messages' && !this.liveSource) {
        this.loadConversations();
        if (this.selectedConversationId !== null) {
          this.loadMessages(this.selectedConversationId);
        }
      }
    }, 4000);
  }

  ngOnDestroy(): void {
    this.closeGifPicker();
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
    if (this.liveSource) {
      this.liveSource.close();
      this.liveSource = null;
    }
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
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
        this.loadError = messageFromHttpError(err, 'Impossible de charger la liste dâ€™amis.');
      },
    });
  }

  topTab(tab: 'network' | 'messages' | 'invitations'): void {
    this.currentTopTab = tab;
    if (tab !== 'messages') {
      this.closeGifPicker();
    }
    if (tab === 'messages') {
      this.loadConversations();
    }
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

  private loadSuggestedUsers(): void {
    this.searching = true;
    this.api.discover().subscribe({
      next: (rows) => {
        this.searching = false;
        this.results = rows;
      },
      error: (err) => {
        this.searching = false;
        this.searchError = messageFromHttpError(err, 'Impossible de charger les comptes utilisateurs.');
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

  activityLabel(user: { activeNow: boolean | null; lastActiveAt: string | null }): string {
    if (user.activeNow === null) {
      return 'Active status hidden';
    }
    if (user.activeNow) {
      return 'Active now';
    }
    if (user.lastActiveAt) {
      const dt = new Date(user.lastActiveAt);
      if (!Number.isNaN(dt.getTime())) {
        return `Last active ${dt.toLocaleString()}`;
      }
    }
    return 'Last active recently';
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

  get currentUserName(): string {
    const first = this.myProfile?.firstName?.trim() ?? '';
    const last = this.myProfile?.lastName?.trim() ?? '';
    const full = `${first} ${last}`.trim();
    if (full) {
      return full;
    }
    const username = this.auth.auth()?.username ?? 'User';
    return username.includes('@') ? username.split('@')[0] : username;
  }

  get currentUserEmail(): string {
    return this.auth.auth()?.email ?? '';
  }

  selectConversation(id: number): void {
    this.closeGifPicker();
    this.selectedConversationId = id;
    this.resetConversationSummaryStateIfNeeded(id);
    this.loadMessages(id);
  }

  openSelectedConversationProfile(): void {
    const thread = this.selectedConversation;
    if (!thread) {
      return;
    }
    // Open modal immediately with safe fallback data,
    // then enrich from backend when available.
    this.selectedFriendProfile = this.buildFallbackProfileFromConversation(thread);
    this.openFriendProfile(thread.otherUserId);
  }

  summarizeSelectedConversation(): void {
    const thread = this.selectedConversation;
    if (!thread || this.summarizingConversation) {
      return;
    }
    this.summarizingConversation = true;
    this.conversationSummaryError = '';
    this.conversationSummary = '';
    this.summaryConversationId = thread.id;
    this.messaging.summarizeConversation(thread.id).subscribe({
      next: (res) => {
        this.summarizingConversation = false;
        this.summaryConversationId = thread.id;
        const analyzed = res.analyzedTextMessages;
        this.conversationSummary =
          (res.summary?.trim() || 'No text messages available for summary.') +
          `\n\n(Analyzed text messages: ${analyzed})`;
      },
      error: (err) => {
        this.summarizingConversation = false;
        this.summaryConversationId = thread.id;
        this.conversationSummaryError = messageFromHttpError(
          err,
          'Unable to summarize this conversation right now.',
        );
      },
    });
  }

  toggleMuteSelectedConversation(): void {
    const thread = this.selectedConversation;
    if (!thread) {
      return;
    }
    const userId = thread.otherUserId;
    if (this.mutedUserIds.has(userId)) {
      this.mutedUserIds.delete(userId);
      this.showToast(`${this.displayConversationName(thread)} unmuted.`);
    } else {
      this.mutedUserIds.add(userId);
      this.showToast(`${this.displayConversationName(thread)} muted.`);
    }
    this.persistMutedUsers();
  }

  isConversationMuted(thread: ConversationResponse): boolean {
    return this.mutedUserIds.has(thread.otherUserId);
  }

  get sentRequests(): FriendSearchResponse[] {
    return this.results.filter((r) => r.relation === 'REQUEST_SENT');
  }

  get onlineFriends(): FriendResponse[] {
    return this.list.filter((f) => f.activeNow === true);
  }

  setNetworkSection(section: 'all' | 'online'): void {
    this.selectedNetworkSection = section;
  }

  openChatWithFriend(friendId: number): void {
    this.currentTopTab = 'messages';
    this.messaging.ensureConversation(friendId).subscribe({
      next: (conversation) => {
        this.selectedConversationId = conversation.id;
        this.loadConversations(friendId);
        this.loadMessages(conversation.id);
      },
      error: (err) => {
        this.requestError = messageFromHttpError(err, 'Impossible d’ouvrir cette conversation.');
      },
    });
  }

  openFriendProfile(friendId: number): void {
    this.loadingFriendProfile = true;
    this.profileLoadError = '';
    this.api.profile(friendId).subscribe({
      next: (profile) => {
        this.loadingFriendProfile = false;
        this.selectedFriendProfile = profile;
      },
      error: (err) => {
        this.loadingFriendProfile = false;
        // Keep modal usable with fallback data: don't show raw 500 text.
        if (err instanceof HttpErrorResponse && err.status >= 500) {
          this.profileLoadError = '';
          return;
        }
        this.profileLoadError = messageFromHttpError(err, 'Some profile details are temporarily unavailable.');
      },
    });
  }

  closeFriendProfile(): void {
    this.selectedFriendProfile = null;
    this.profileLoadError = '';
  }

  get visibleConnections(): FriendResponse[] {
    return this.selectedNetworkSection === 'online' ? this.onlineFriends : this.list;
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

  sendCurrentMessage(): void {
    const thread = this.selectedConversation;
    const content = this.messageDraft.trim();
    if (!thread || !content) {
      return;
    }
    this.requestError = '';
    this.sendingMessage = true;
    this.messaging.send(thread.otherUserId, content, this.replyingToMessage?.id ?? null).subscribe({
      next: () => {
        this.sendingMessage = false;
        this.messageDraft = '';
        this.replyingToMessage = null;
        this.loadMessages(thread.id);
        this.loadConversations();
      },
      error: (err) => {
        this.sendingMessage = false;
        this.requestError = messageFromHttpError(err, 'Envoi du message impossible.');
      },
    });
  }

  onGifButtonActivate(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.openGifPicker();
  }

  openGifPicker(): void {
    const thread = this.selectedConversation;
    if (!thread) {
      this.requestError = 'Open a conversation first.';
      return;
    }
    this.requestError = '';
    this.gifPickerOpen = true;
    this.gifSearchQuery = '';
    this.gifResults = [];
    this.gifLoading = false;
    this.gifError = '';
    this.gifInfo = 'Loading trending GIFs...';
    this.loadGiphyTrending();
  }

  closeGifPicker(): void {
    this.gifPickerOpen = false;
    this.gifLoading = false;
    this.gifError = '';
    this.gifInfo = '';
  }

  /** Trending GIFs load as soon as the picker opens (Giphy “home” experience). */
  loadGiphyTrending(): void {
    const apiKey = environment.giphyApiKey?.trim();
    if (!apiKey) {
      this.gifError = 'Giphy API key is missing. Add giphyApiKey in src/environments/environment*.ts';
      return;
    }
    const params = new HttpParams().set('api_key', apiKey).set('limit', '24').set('rating', 'g');
    this.gifLoading = true;
    this.gifResults = [];
    this.gifError = '';
    this.gifInfo = 'Loading trending GIFs...';
    this.http
      .get<GiphySearchResponse>('https://api.giphy.com/v1/gifs/trending', { params })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (payload) => {
          this.gifLoading = false;
          this.gifResults = this.mapGiphyPayloadToResults(payload);
          this.gifInfo = this.gifResults.length > 0 ? 'Pick one to send.' : '';
          if (this.gifResults.length === 0) {
            this.gifError = 'No trending GIFs returned from Giphy.';
          }
        },
        error: (err: HttpErrorResponse) => {
          this.gifLoading = false;
          this.gifError = `${this.giphyHttpErrorMessage(err)} Showing fallback GIFs.`;
          this.gifResults = [...FALLBACK_GIFS];
          this.gifInfo = 'Fallback GIFs loaded.';
        },
      });
  }

  searchGifs(): void {
    const apiKey = environment.giphyApiKey?.trim();
    if (!apiKey) {
      this.gifError = 'Giphy API key is missing. Add giphyApiKey in src/environments/environment*.ts';
      return;
    }
    const q = this.gifSearchQuery.trim();
    if (!q) {
      this.loadGiphyTrending();
      return;
    }
    const params = new HttpParams().set('api_key', apiKey).set('q', q).set('limit', '24').set('rating', 'g');
    this.gifLoading = true;
    this.gifResults = [];
    this.gifError = '';
    this.gifInfo = `Searching "${q}"...`;
    this.http
      .get<GiphySearchResponse>('https://api.giphy.com/v1/gifs/search', { params })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (payload) => {
          this.gifLoading = false;
          this.gifResults = this.mapGiphyPayloadToResults(payload);
          this.gifInfo = this.gifResults.length > 0 ? 'Pick one to send.' : '';
          if (this.gifResults.length === 0) {
            this.gifError = 'No GIFs found for this search.';
          }
        },
        error: (err: HttpErrorResponse) => {
          this.gifLoading = false;
          this.gifError = `${this.giphyHttpErrorMessage(err)} Showing fallback GIFs.`;
          this.gifResults = [...FALLBACK_GIFS];
          this.gifInfo = 'Fallback GIFs loaded.';
        },
      });
  }

  private mapGiphyPayloadToResults(payload: GiphySearchResponse): GifPickerResultItem[] {
    const rows: GifPickerResultItem[] = [];
    for (const item of payload.data ?? []) {
      const id = String(item.id ?? '').trim();
      const url =
        item.images?.original?.url?.trim() ||
        item.images?.original?.webp?.trim() ||
        '';
      if (!id || !url) {
        continue;
      }
      const preview =
        item.images?.fixed_height_small?.url?.trim() ||
        item.images?.fixed_height?.url?.trim() ||
        url;
      rows.push({ id, url, previewUrl: preview });
    }
    return rows;
  }

  private giphyHttpErrorMessage(err: HttpErrorResponse): string {
    if (err.status === 401 || err.status === 403) {
      return 'Giphy rejected the request (invalid or restricted API key).';
    }
    if (err.status === 429) {
      return 'Giphy rate limit reached. Try again in a minute.';
    }
    return 'Unable to reach Giphy. Check your network and API key.';
  }

  selectGifFromPicker(gifUrl: string): void {
    const thread = this.selectedConversation;
    if (!thread) {
      return;
    }
    this.closeGifPicker();
    this.sendingMessage = true;
    this.messaging.send(thread.otherUserId, `${FriendsComponent.GIF_MARKER}${gifUrl}`, this.replyingToMessage?.id ?? null).subscribe({
      next: () => {
        this.sendingMessage = false;
        this.replyingToMessage = null;
        this.loadMessages(thread.id);
        this.loadConversations();
      },
      error: (err) => {
        this.sendingMessage = false;
        this.requestError = messageFromHttpError(err, 'Unable to send GIF message.');
      },
    });
  }

  setReplyTarget(message: DirectMessageResponse): void {
    this.replyingToMessage = message;
    this.focusComposerInput();
  }

  clearReplyTarget(): void {
    this.replyingToMessage = null;
    this.focusComposerInput();
  }

  toggleVoiceRecording(): void {
    if (this.recordingVoice) {
      this.stopVoiceRecordingAndSend();
      return;
    }
    this.startVoiceRecording();
  }

  private startVoiceRecording(): void {
    const thread = this.selectedConversation;
    if (!thread) {
      this.requestError = 'Open a conversation first.';
      return;
    }
    navigator.mediaDevices
      .getUserMedia({ audio: true })
      .then((stream) => {
        this.recordedChunks = [];
        this.mediaRecorder = new MediaRecorder(stream);
        this.mediaRecorder.ondataavailable = (event) => {
          if (event.data.size > 0) {
            this.recordedChunks.push(event.data);
          }
        };
        this.mediaRecorder.onstop = () => {
          const blob = new Blob(this.recordedChunks, { type: this.mediaRecorder?.mimeType || 'audio/webm' });
          stream.getTracks().forEach((t) => t.stop());
          this.sendRecordedVoice(blob);
          this.mediaRecorder = null;
          this.recordedChunks = [];
        };
        this.mediaRecorder.start();
        this.recordingVoice = true;
      })
      .catch(() => {
        this.requestError = 'Microphone access denied.';
      });
  }

  private stopVoiceRecordingAndSend(): void {
    if (!this.mediaRecorder) {
      this.recordingVoice = false;
      return;
    }
    this.recordingVoice = false;
    this.mediaRecorder.stop();
  }

  private sendRecordedVoice(blob: Blob): void {
    const thread = this.selectedConversation;
    if (!thread) {
      return;
    }
    this.sendingMessage = true;
    this.messaging.sendVoice(blob, thread.otherUserId, this.replyingToMessage?.id ?? null).subscribe({
      next: () => {
        this.sendingMessage = false;
        this.replyingToMessage = null;
        this.loadMessages(thread.id);
        this.loadConversations();
      },
      error: (err) => {
        this.sendingMessage = false;
        this.requestError = messageFromHttpError(err, 'Unable to send voice message.');
      },
    });
  }

  composerPlaceholder(): string {
    return this.replyingToMessage ? `Reply to ${this.replyingToMessage.senderUsername}...` : 'Aa';
  }

  reactToMessage(message: DirectMessageResponse, emoji = '🙂'): void {
    this.messaging.react(message.id, emoji).subscribe({
      next: () => {
        if (this.selectedConversationId !== null) {
          this.loadMessages(this.selectedConversationId);
        }
      },
      error: (err) => {
        this.requestError = messageFromHttpError(err, 'Unable to react to this message.');
      },
    });
  }

  toggleDeleteMenu(message: DirectMessageResponse): void {
    this.deleteMenuMessageId = this.deleteMenuMessageId === message.id ? null : message.id;
  }

  closeDeleteMenu(): void {
    this.deleteMenuMessageId = null;
  }

  deleteMessage(message: DirectMessageResponse, scope: 'ME' | 'EVERYONE'): void {
    this.deleteMenuMessageId = null;
    this.messaging.deleteMessage(message.id, scope).subscribe({
      next: () => {
        if (this.replyingToMessage?.id === message.id) {
          this.replyingToMessage = null;
        }
        if (this.selectedConversationId !== null) {
          this.loadMessages(this.selectedConversationId);
          this.loadConversations();
        }
      },
      error: (err) => {
        this.requestError = messageFromHttpError(err, 'Unable to delete this message.');
      },
    });
  }

  canDeleteForEveryone(message: DirectMessageResponse): boolean {
    return message.senderId === this.currentUserId;
  }

  isGifMessage(message: DirectMessageResponse): boolean {
    return !!message.content?.startsWith(FriendsComponent.GIF_MARKER);
  }

  gifUrlFromMessage(message: DirectMessageResponse): string {
    if (!this.isGifMessage(message)) {
      return '';
    }
    return message.content.slice(FriendsComponent.GIF_MARKER.length).trim();
  }

  replyMetaText(message: DirectMessageResponse): string {
    const target = this.replyTargetName(message);
    if (message.senderId === this.currentUserId) {
      return `You replied to ${target}`;
    }
    return `${message.senderUsername} replied to ${target}`;
  }

  private replyTargetName(message: DirectMessageResponse): string {
    if (!message.replyToMessageId) {
      return 'this message';
    }
    const target = this.currentMessages.find((m) => m.id === message.replyToMessageId);
    if (target?.senderId === this.currentUserId) {
      return 'you';
    }
    return target?.senderUsername || 'this message';
  }

  get selectedConversation(): ConversationResponse | null {
    if (this.conversations.length === 0 || this.selectedConversationId === null) {
      return null;
    }
    return this.conversations.find((c) => c.id === this.selectedConversationId) ?? null;
  }

  get currentUserId(): number | null {
    return this.auth.auth()?.userId ?? null;
  }

  displayConversationName(thread: ConversationResponse): string {
    const full = `${thread.otherFirstName ?? ''} ${thread.otherLastName ?? ''}`.trim();
    return full || thread.otherUsername;
  }

  profileDisplayName(profile: FriendProfileResponse): string {
    const full = `${profile.firstName ?? ''} ${profile.lastName ?? ''}`.trim();
    if (full) {
      return full;
    }
    const username = profile.username ?? '';
    if (username.includes('@')) {
      return username.split('@')[0];
    }
    return username || 'User';
  }

  profilePictureUrl(stored: string | null | undefined): string {
    const value = stored?.trim() ?? '';
    if (!value) {
      return '';
    }
    const preset = resolvePresetProfilePicture(value);
    if (preset) {
      return preset;
    }
    if (/^https?:\/\//i.test(value)) {
      return value;
    }
    if (value.startsWith('/api/')) {
      const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
      return base ? `${base}${value}` : value;
    }
    return '';
  }

  messageVoiceUrl(stored: string | null | undefined): string {
    const value = stored?.trim() ?? '';
    if (!value) {
      return '';
    }
    if (/^https?:\/\//i.test(value)) {
      return value;
    }
    if (value.startsWith('/api/')) {
      const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
      return base ? `${base}${value}` : value;
    }
    return '';
  }

  conversationPictureUrl(thread: ConversationResponse): string {
    const fromFriends = this.list.find((f) => f.id === thread.otherUserId)?.profilePicture;
    if (fromFriends) {
      return this.profilePictureUrl(fromFriends);
    }
    const fromSearch = this.results.find((r) => r.id === thread.otherUserId)?.profilePicture;
    if (fromSearch) {
      return this.profilePictureUrl(fromSearch);
    }
    const fromIncoming = this.incomingRequests.find((r) => r.sender.id === thread.otherUserId)?.sender.profilePicture;
    if (fromIncoming) {
      return this.profilePictureUrl(fromIncoming);
    }
    return '';
  }

  fullNameFromConversation(thread: ConversationResponse): string {
    const full = `${thread.otherFirstName ?? ''} ${thread.otherLastName ?? ''}`.trim();
    return full || `@${thread.otherUsername}`;
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

  private buildFallbackProfileFromConversation(thread: ConversationResponse): FriendProfileResponse {
    return {
      id: thread.otherUserId,
      username: thread.otherUsername,
      firstName: thread.otherFirstName ?? null,
      lastName: thread.otherLastName ?? null,
      description: null,
      interests: [],
      profilePicture:
        this.list.find((f) => f.id === thread.otherUserId)?.profilePicture ??
        this.results.find((r) => r.id === thread.otherUserId)?.profilePicture ??
        null,
      activeNow: thread.otherActiveNow ?? null,
      lastActiveAt: thread.otherLastActiveAt ?? null,
    };
  }

  private hydrateMutedUsers(): void {
    try {
      const raw = localStorage.getItem(FriendsComponent.MUTED_USERS_STORAGE_KEY);
      if (!raw) {
        return;
      }
      const parsed = JSON.parse(raw);
      if (!Array.isArray(parsed)) {
        return;
      }
      this.mutedUserIds = new Set(
        parsed.map((v) => Number(v)).filter((v) => Number.isInteger(v) && v > 0),
      );
    } catch {
      this.mutedUserIds = new Set<number>();
    }
  }

  private persistMutedUsers(): void {
    localStorage.setItem(FriendsComponent.MUTED_USERS_STORAGE_KEY, JSON.stringify(Array.from(this.mutedUserIds)));
  }

  private focusComposerInput(): void {
    setTimeout(() => this.composerInput?.nativeElement.focus(), 0);
  }

  private loadConversations(preferredUserId?: number): void {
    this.loadingConversations = true;
    this.messaging.conversations().subscribe({
      next: (rows) => {
        this.loadingConversations = false;
        this.conversations = rows;
        if (rows.length === 0) {
          this.selectedConversationId = null;
          this.currentMessages = [];
          return;
        }
        if (preferredUserId != null) {
          const preferred = rows.find((r) => r.otherUserId === preferredUserId);
          if (preferred) {
            this.selectedConversationId = preferred.id;
            this.loadMessages(preferred.id);
            return;
          }
        }
        const hasValidSelection =
          this.selectedConversationId !== null && rows.some((r) => r.id === this.selectedConversationId);
        if (!hasValidSelection) {
          this.selectedConversationId = rows[0].id;
          this.loadMessages(rows[0].id);
        }
      },
      error: (err) => {
        this.loadingConversations = false;
        this.loadError = messageFromHttpError(err, 'Impossible de charger les conversations.');
      },
    });
  }

  private loadMessages(conversationId: number): void {
    this.loadingMessages = true;
    this.messaging.messages(conversationId).subscribe({
      next: (rows) => {
        this.loadingMessages = false;
        this.currentMessages = rows;
      },
      error: (err) => {
        this.loadingMessages = false;
        this.loadError = messageFromHttpError(err, 'Impossible de charger les messages.');
      },
    });
  }

  private resetConversationSummaryStateIfNeeded(conversationId: number): void {
    if (this.summaryConversationId === conversationId) {
      return;
    }
    this.summarizingConversation = false;
    this.conversationSummary = '';
    this.conversationSummaryError = '';
    this.summaryConversationId = null;
  }

  private initLiveMessaging(): void {
    const token = this.auth.token();
    if (!token) {
      return;
    }
    this.liveSource = this.messaging.stream(token);
    this.liveSource.addEventListener('message', () => {
      this.loadConversations();
      if (this.selectedConversationId !== null) {
        this.loadMessages(this.selectedConversationId);
      }
    });
    this.liveSource.onerror = () => {
      if (this.liveSource) {
        this.liveSource.close();
        this.liveSource = null;
      }
      // Fallback: refresh once while disconnected.
      if (this.currentTopTab === 'messages') {
        this.loadConversations();
        if (this.selectedConversationId !== null) {
          this.loadMessages(this.selectedConversationId);
        }
      }
      setTimeout(() => this.initLiveMessaging(), 1800);
    };
  }

  private loadCurrentProfileName(): void {
    this.profileApi.getMe().subscribe({
      next: (profile: ProfileResponse) => {
        this.myProfile = profile;
      },
      error: () => {
        // Keep chat usable even if profile details fail to load.
      },
    });
  }
}

