import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SocialLoginComponent } from '../../components/social-login/social-login.component';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, SocialLoginComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component\.css',
})
export class HomeComponent {
  protected readonly auth = inject(AuthService);
  protected readonly hasOAuthClients = Boolean(
    environment.googleClientId?.trim() || environment.facebookAppId?.trim(),
  );
}

