import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import type { SubscriptionPlan, SubscriptionResponse } from '../../core/models/api.models';
import { SubscriptionService } from '../../core/services/subscription.service';
import { messageFromHttpError } from '../../core/util/http-error';

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './subscriptions.component.html',
  styleUrl: './subscriptions.component.scss',
})
export class SubscriptionsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(SubscriptionService);

  readonly plans: { value: SubscriptionPlan; label: string }[] = [
    { value: 'FREE', label: 'Gratuit' },
    { value: 'STANDARD', label: 'Standard' },
    { value: 'PREMIUM', label: 'Premium' },
  ];

  readonly createForm = this.fb.nonNullable.group({
    subscriptionPlan: this.fb.nonNullable.control<SubscriptionPlan>('FREE', Validators.required),
    dateOfSubscription: [''],
    expirationDate: ['', Validators.required],
    billingDate: [''],
  });

  list: SubscriptionResponse[] = [];
  loadError = '';
  createError = '';
  loading = false;
  creating = false;

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.loadError = '';
    this.api.listMine().subscribe({
      next: (rows) => {
        this.loading = false;
        this.list = rows;
      },
      error: (err) => {
        this.loading = false;
        this.loadError = messageFromHttpError(err, 'Impossible de charger les abonnements.');
      },
    });
  }

  create(): void {
    this.createError = '';
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    const v = this.createForm.getRawValue();
    this.creating = true;
    this.api
      .create({
        subscriptionPlan: v.subscriptionPlan,
        dateOfSubscription: v.dateOfSubscription || null,
        expirationDate: v.expirationDate,
        billingDate: v.billingDate || null,
      })
      .subscribe({
        next: () => {
          this.creating = false;
          this.createForm.patchValue({
            dateOfSubscription: '',
            expirationDate: '',
            billingDate: '',
          });
          this.refresh();
        },
        error: (err) => {
          this.creating = false;
          this.createError = messageFromHttpError(err, 'Création impossible.');
        },
      });
  }
}
