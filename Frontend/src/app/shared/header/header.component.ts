import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CurrentUserService } from '../../core/auth/current-user.service';

declare var $: any;

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  users$;
  currentUser$;

  constructor(
    private currentUserService: CurrentUserService,
    private router: Router
  ) {
    this.users$ = this.currentUserService.users$;
    this.currentUser$ = this.currentUserService.currentUser$;
  }

  onUserChange(userIdValue: string): void {
    this.currentUserService.switchUser(Number(userIdValue));
    this.router.navigate(['/']);
  }
}
