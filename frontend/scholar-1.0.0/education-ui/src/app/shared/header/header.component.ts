import { Component } from '@angular/core';
import { CurrentUserService } from '../../core/auth/current-user.service';

declare var $: any;

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  currentUser$;
  availableUsers$;

  constructor(private currentUserService: CurrentUserService) {
    this.currentUser$ = this.currentUserService.currentUser$;
    this.availableUsers$ = this.currentUserService.availableUsers$;
  }

  onSessionChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const selectedId = Number(target.value);
    this.currentUserService.selectUser(selectedId);
  }
}
