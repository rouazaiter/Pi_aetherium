import { AfterViewInit, Component, OnDestroy, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import AOS from 'aos';
import { SocialLoginComponent } from '../../components/social-login/social-login.component';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, SocialLoginComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements AfterViewInit, OnDestroy {
  protected readonly auth = inject(AuthService);
  protected readonly hasOAuthClients = Boolean(
    environment.googleClientId?.trim() || environment.facebookAppId?.trim(),
  );

  protected readonly heroStats = [
    { value: '50K+', label: 'Active learners' },
    { value: '120+', label: 'Hands-on labs' },
    { value: '96%', label: 'Completion success' },
    { value: '24/7', label: 'AI support' },
  ];

  protected readonly featureHighlights = [
    {
      icon: 'LL',
      title: 'Industry-led curriculum',
      description: 'Structured learning paths built by architects, security experts, and senior mentors.',
    },
    {
      icon: 'HL',
      title: 'Hands-on labs',
      description: 'Real-world sandbox environments to practice delivery, testing, and cloud workflows.',
    },
    {
      icon: 'CR',
      title: 'Certification track',
      description: 'Milestone-based validation that strengthens your portfolio and career credibility.',
    },
  ];

  protected readonly instructors = [
    {
      initials: 'AS',
      name: 'Aicha Smaoui',
      role: 'Senior Cloud Architect',
      bio: 'Former AWS principal architect with deep experience in resilient distributed systems.',
    },
    {
      initials: 'GK',
      name: 'Ghassen Kahia',
      role: 'Security Specialist',
      bio: 'Hands-on leader in enterprise security programs and secure engineering practices.',
    },
    {
      initials: 'MA',
      name: 'Mohamed Arous',
      role: 'ML Engineer',
      bio: 'Designs practical ML pipelines and real-time data platforms for production teams.',
    },
    {
      initials: 'IB',
      name: 'Jihen Ben Fraj',
      role: 'DevOps Consultant',
      bio: 'Expert in Kubernetes orchestration, CI/CD hardening, and release reliability.',
    },
    {
      initials: 'RZ',
      name: 'Roua Zeiter',
      role: 'Full-stack Lead',
      bio: 'Mentors modern web engineering with strong API architecture and frontend delivery.',
    },
    {
      initials: 'OB',
      name: 'Oussema Bouasker',
      role: 'UX Architect',
      bio: 'Builds product experiences that balance usability, accessibility, and growth outcomes.',
    },
    {
      initials: 'MB',
      name: 'Meriem Bouhajeb',
      role: 'Data Expert',
      bio: 'Specialized in high-volume SQL and NoSQL workloads with performance-first design.',
    },
  ];

  ngAfterViewInit(): void {
    AOS.init({
      duration: 800,
      once: true,
      easing: 'ease-out-cubic',
      offset: 40,
    });
    AOS.refreshHard();
  }

  ngOnDestroy(): void {
    // Keep cleanup explicit if page is re-mounted multiple times.
    AOS.refresh();
  }
}

