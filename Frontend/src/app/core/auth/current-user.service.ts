import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface TestUser {
  id: number;
  username: string;
  email: string;
}

const STORAGE_KEY = 'skillhub.test.currentUserId';

@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  readonly testUsers: TestUser[] = [
    { id: 1, username: 'client.demo', email: 'client.demo@skillhub.test' },
    { id: 2, username: 'freelancer.demo', email: 'freelancer.demo@skillhub.test' }
  ];

  private readonly currentUserSubject = new BehaviorSubject<TestUser>(this.resolveInitialUser());

  readonly currentUser$ = this.currentUserSubject.asObservable();

  get currentUser(): TestUser {
    return this.currentUserSubject.value;
  }

  switchUser(userId: number): void {
    const nextUser = this.testUsers.find(user => user.id === userId);
    if (!nextUser) {
      return;
    }

    sessionStorage.setItem(STORAGE_KEY, String(nextUser.id));
    this.currentUserSubject.next(nextUser);
  }

  private resolveInitialUser(): TestUser {
    const storedId = Number(sessionStorage.getItem(STORAGE_KEY));
    const storedUser = this.testUsers.find(user => user.id === storedId);
    if (storedUser) {
      return storedUser;
    }

    sessionStorage.setItem(STORAGE_KEY, String(this.testUsers[0].id));
    return this.testUsers[0];
  }
}
