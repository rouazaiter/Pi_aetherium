import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import type {
  SubscriptionPlan,
  SubscriptionPlanResponse,
  SubscriptionResponse,
} from '../../core/models/api.models';
import { SubscriptionService } from '../../core/services/subscription.service';
import { messageFromHttpError } from '../../core/util/http-error';

type PlanUiConfig = {
  tier: string;
  label: string;
  highlighted: boolean;
  description: string[];
};

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, DecimalPipe],
  templateUrl: './subscriptions.component.html',
  styleUrl: './subscriptions.component.scss',
})
export class SubscriptionsComponent implements OnInit {
  private readonly api = inject(SubscriptionService);
  private readonly router = inject(Router);
  readonly actionCards = [
    {
      title: 'Finish Profile Security',
      description: 'Enable 2FA and verify recovery contact for stronger account protection.',
      cta: 'Secure Profile',
      tone: 'security',
      action: 'profile',
    },
    {
      title: 'Join One Challenge',
      description: 'Enter a group challenge this week to boost consistency and momentum.',
      cta: 'Open Challenges',
      tone: 'challenge',
      action: 'challenges',
    },
    {
      title: 'Review Last Session',
      description: 'Rewatch your latest learning session and capture action notes.',
      cta: 'Review Session',
      tone: 'session',
      action: 'review',
    },
    {
      title: 'Invite a Friend',
      description: 'Bring one friend to your study circle and unlock social accountability.',
      cta: 'Send Invite',
      tone: 'social',
      action: 'friends',
    },
    {
      title: 'Upgrade Recommendation',
      description: 'Premium unlocks advanced analytics and priority expert support.',
      cta: 'See Upgrade',
      tone: 'upgrade',
      action: 'upgrade',
    },
  ] as const;

  private readonly uiConfig: Record<SubscriptionPlan, PlanUiConfig> = {
    STANDARD: {
      tier: 'STANDARD',
      label: 'Free Tier',
      highlighted: false,
      description: [
        'Up to 10 active projects',
        'Basic analytics dashboard',
        'Community support',
        'Advanced AI insights',
      ],
    },
    PREMIUM: {
      tier: 'PREMIUM',
      label: 'Business Pro',
      highlighted: true,
      description: [
        'Unlimited Projects',
        'Priority Support (24/7)',
        'Advanced Analytics',
        'Custom API access',
      ],
    },
  };

  plans: SubscriptionPlanResponse[] = [];
  list: SubscriptionResponse[] = [];
  current: SubscriptionResponse | null = null;

  plansError = '';
  loadError = '';
  actionError = '';
  loadingPlans = false;
  loadingList = false;
  buyingPlan: SubscriptionPlan | null = null;
  downloadingInvoiceId: number | null = null;
  planFilter: 'ALL' | SubscriptionPlan = 'ALL';

  ngOnInit(): void {
    this.loadPlans();
    this.refresh();
  }

  loadPlans(): void {
    this.loadingPlans = true;
    this.plansError = '';
    this.api.listPlans().subscribe({
      next: (rows) => {
        this.loadingPlans = false;
        this.plans = rows.filter((p) => p.plan === 'STANDARD' || p.plan === 'PREMIUM');
      },
      error: (err) => {
        this.loadingPlans = false;
        this.plansError = messageFromHttpError(err, 'Impossible de charger les offres.');
      },
    });
  }

  refresh(): void {
    this.loadingList = true;
    this.loadError = '';
    this.api.listMine().subscribe({
      next: (rows) => {
        this.loadingList = false;
        this.list = rows;
        this.current = rows.find((s) => s.status === 'ACTIVE') ?? null;
      },
      error: (err) => {
        this.loadingList = false;
        this.loadError = messageFromHttpError(err, 'Impossible de charger les abonnements.');
      },
    });
  }

  buy(plan: SubscriptionPlan): void {
    this.actionError = '';
    this.buyingPlan = plan;
    this.api
      .create({
        subscriptionPlan: plan,
        autoRenew: true,
      })
      .subscribe({
        next: () => {
          this.buyingPlan = null;
          this.refresh();
        },
        error: (err) => {
          this.buyingPlan = null;
          this.actionError = messageFromHttpError(err, 'Creation impossible.');
        },
      });
  }

  getPlanUi(plan: SubscriptionPlan): PlanUiConfig {
    return this.uiConfig[plan];
  }

  amountFor(plan: SubscriptionPlan): number {
    return this.plans.find((p) => p.plan === plan)?.monthlyPrice ?? 0;
  }

  planLabel(plan: SubscriptionPlan): string {
    return this.getPlanUi(plan).label;
  }

  setPlanFilter(value: 'ALL' | SubscriptionPlan): void {
    this.planFilter = value;
  }

  get filteredHistory(): SubscriptionResponse[] {
    const scoped =
      this.planFilter === 'ALL' ? this.list : this.list.filter((s) => s.subscriptionPlan === this.planFilter);
    return [...scoped].sort((a, b) => {
      const dateA = this.toTimestamp(a);
      const dateB = this.toTimestamp(b);
      return dateB - dateA;
    });
  }

  private toTimestamp(s: SubscriptionResponse): number {
    const source = s.dateOfSubscription ?? s.billingDate ?? s.expirationDate ?? null;
    if (!source) {
      return 0;
    }
    const value = new Date(source).getTime();
    return Number.isNaN(value) ? 0 : value;
  }

  onActionCardClick(action: (typeof this.actionCards)[number]['action']): void {
    switch (action) {
      case 'profile':
        void this.router.navigateByUrl('/profile');
        break;
      case 'challenges':
        void this.router.navigateByUrl('/social-hub?tab=challenges');
        break;
      case 'review':
        void this.router.navigateByUrl('/social-hub?tab=feed');
        break;
      case 'friends':
        void this.router.navigateByUrl('/friends');
        break;
      case 'upgrade':
        window.scrollTo({ top: 0, behavior: 'smooth' });
        break;
      default:
        break;
    }
  }

  downloadInvoice(subscriptionId: number): void {
    this.actionError = '';
    this.downloadingInvoiceId = subscriptionId;
    this.api.downloadInvoice(subscriptionId).subscribe({
      next: (blob) => {
        this.downloadingInvoiceId = null;
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `invoice-${subscriptionId}.html`;
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.downloadingInvoiceId = null;
        this.actionError = messageFromHttpError(err, 'Unable to download invoice.');
      },
    });
  }
}

