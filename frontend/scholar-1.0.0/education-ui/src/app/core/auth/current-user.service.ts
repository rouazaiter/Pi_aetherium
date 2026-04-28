import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

export interface CurrentSessionUser {
  id: number;
  username: string;
  email: string;
}

const STORAGE_KEY = 'skillhub.currentUserId';

@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private readonly currentUserSubject = new BehaviorSubject<CurrentSessionUser>({
    id: 0,
    username: 'loading...',
    email: ''
  });

  readonly currentUser$ = this.currentUserSubject.asObservable();
  private readonly availableUsersSubject = new BehaviorSubject<CurrentSessionUser[]>([]);
  readonly availableUsers$ = this.availableUsersSubject.asObservable();

  get currentUser(): CurrentSessionUser {
    return this.currentUserSubject.value;
  }

  selectUser(userId: number): void {
    const currentUsers = this.availableUsersSubject.value;
    const selected = currentUsers.find(user => user.id === userId);
    if (selected) {
      sessionStorage.setItem(STORAGE_KEY, String(selected.id));
      this.currentUserSubject.next(selected);
    }
  }

  constructor(private http: HttpClient) {
    this.loadCurrentUser();
  }

  private loadCurrentUser(): void {
    this.http.get<CurrentSessionUser[]>('/skillhub/api/users').subscribe({
      next: (users) => {
        const availableUsers = (users ?? []).filter(user => !!user?.id);

        if (availableUsers.length === 0) {
          this.currentUserSubject.next({ id: 0, username: 'No user', email: '' });
          return;
        }

        const storedId = Number(sessionStorage.getItem(STORAGE_KEY));
        this.availableUsersSubject.next(availableUsers);
        const selected = availableUsers.find(user => user.id === storedId) ?? availableUsers[0];

        sessionStorage.setItem(STORAGE_KEY, String(selected.id));
        this.currentUserSubject.next(selected);
      },
      error: () => {
        this.currentUserSubject.next({ id: 0, username: 'No user', email: '' });
        this.availableUsersSubject.next([]);
      }
    });
  }
}
