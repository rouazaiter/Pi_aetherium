import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ApplicationService } from '../../../core/services/application.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { CurrentUserService } from '../../../core/auth/current-user.service';

@Component({
  selector: 'app-apply-form',
  templateUrl: './apply-form.component.html',
  styleUrls: ['./apply-form.component.css']
})
export class ApplyFormComponent implements OnInit {
  serviceRequest?: ServiceRequest;
  form!: FormGroup;
  loading = false;
  error = '';
  success = '';
  alreadyApplied = false;
  currentUserId = 1;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private srService: ServiceRequestService,
    private appService: ApplicationService,
    private currentUserService: CurrentUserService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.currentUserService.currentUser.id;
    this.currentUserService.currentUser$.subscribe(user => this.currentUserId = user.id);

    const id = Number(this.route.snapshot.params['id']);

    this.form = this.fb.group({
      message: ['', [Validators.required, Validators.maxLength(2000)]]
    });

    // Load the request
    this.srService.getById(id).subscribe({
      next: (sr) => {
        // If this is my own request, do not allow applying
        if (sr.creator.id === this.currentUserId) {
          this.router.navigate(['/marketplace']);
          return;
        }
        this.serviceRequest = sr;

        // Check whether user already applied
        this.appService.hasApplied(id, this.currentUserId).subscribe({
          next: (res) => this.alreadyApplied = res.hasApplied
        });
      },
      error: () => this.router.navigate(['/marketplace'])
    });
  }

  onSubmit(): void {
    if (this.form.invalid || !this.serviceRequest) return;
    this.loading = true;
    this.error = '';

    this.appService.apply(this.currentUserId, this.serviceRequest.id, this.form.value.message).subscribe({
      next: () => {
        this.success = 'Application submitted successfully.';
        this.alreadyApplied = true;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'An error occurred while submitting your application.';
        this.loading = false;
      }
    });
  }
}
