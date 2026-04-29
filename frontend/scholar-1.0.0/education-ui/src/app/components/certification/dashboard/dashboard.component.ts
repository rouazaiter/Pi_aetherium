import { Component, OnInit } from '@angular/core';
import { CertificationService } from '../services/certification.service';
import { Certification } from '../models/certification.model';
import { RouterLink } from '@angular/router';
import { NgIf, NgFor, CurrencyPipe, DatePipe } from '@angular/common';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss'],
    standalone: true,
    imports: [RouterLink, NgIf, NgFor, CurrencyPipe, DatePipe]
})
export class DashboardComponent implements OnInit {
  certifications: Certification[] = [];
  loading = true;

  get total() { return this.certifications.length; }
  get published() { return this.certifications.filter(c => c.status === 'PUBLISHED').length; }
  get drafts() { return this.certifications.filter(c => c.status === 'DRAFT').length; }
  get archived() { return this.certifications.filter(c => c.status === 'ARCHIVED').length; }
  get recent() {
    return [...this.certifications]
      .sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
      .slice(0, 5);
  }

  constructor(private certService: CertificationService) {}

  ngOnInit(): void {
    this.certService.getAll().subscribe({
      next: data => { this.certifications = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
