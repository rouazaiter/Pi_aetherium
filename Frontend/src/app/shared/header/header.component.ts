import { Component } from '@angular/core';
import { CurrentUserService, TestUser } from '../../core/auth/current-user.service';

declare var $: any;

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  users: TestUser[];
  currentUser$;

  constructor(private currentUserService: CurrentUserService) {
    this.users = this.currentUserService.testUsers;
    this.currentUser$ = this.currentUserService.currentUser$;
  }

  onUserChange(userIdValue: string): void {
    this.currentUserService.switchUser(Number(userIdValue));
  }
}
