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
  private readonly usersSubject = new BehaviorSubject<CurrentSessionUser[]>([]);
  readonly users$ = this.usersSubject.asObservable();

  private readonly currentUserSubject = new BehaviorSubject<CurrentSessionUser>({
    id: 0,
    username: 'loading...',
    email: ''
  });

  readonly currentUser$ = this.currentUserSubject.asObservable();

  get currentUser(): CurrentSessionUser {
    return this.currentUserSubject.value;
  }

  constructor(private http: HttpClient) {
    this.loadUsers();
  }

  switchUser(userId: number): void {
    const nextUser = this.usersSubject.value.find(user => user.id === userId);
    if (!nextUser) {
      return;
    }

    sessionStorage.setItem(STORAGE_KEY, String(nextUser.id));
    this.currentUserSubject.next(nextUser);
  }

  private loadUsers(): void {
    this.http.get<CurrentSessionUser[]>('/skillhub/api/users?limit=2').subscribe({
      next: (users) => {
        const availableUsers = (users ?? []).filter(user => !!user?.id);
        this.usersSubject.next(availableUsers);

        if (availableUsers.length === 0) {
          this.currentUserSubject.next({ id: 0, username: 'No user', email: '' });
          return;
        }

        const storedId = Number(sessionStorage.getItem(STORAGE_KEY));
        const selected = availableUsers.find(user => user.id === storedId) ?? availableUsers[0];

        sessionStorage.setItem(STORAGE_KEY, String(selected.id));
        this.currentUserSubject.next(selected);
      },
      error: () => {
        this.usersSubject.next([]);
        this.currentUserSubject.next({ id: 0, username: 'No user', email: '' });
      }
    });
  }
}
