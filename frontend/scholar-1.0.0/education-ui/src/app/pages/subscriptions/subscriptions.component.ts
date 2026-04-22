import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import type {
  SubscriptionPlan,
  SubscriptionPlanResponse,
  SubscriptionResponse,
} from '../../core/models/api.models';
import { SubscriptionService } from '../../core/services/subscription.service';
import { messageFromHttpError } from '../../core/util/http-error';

type PlanUiConfig = {
  label: string;
  storage: string;
  highlighted: boolean;
  description: string[];
};

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './subscriptions.component.html',
  styleUrl: './subscriptions.component.scss',
})
export class SubscriptionsComponent implements OnInit {
  private readonly api = inject(SubscriptionService);

  private readonly uiConfig: Record<SubscriptionPlan, PlanUiConfig> = {
    STANDARD: {
      label: 'Standard',
      storage: '1 Tb',
      highlighted: false,
      description: [
        'Session securisee avec enregistrement en cas de probleme',
        'Exercices pratiques pour mieux comprendre',
        'Supervision apres la session et contact avec un expert',
      ],
    },
    PREMIUM: {
      label: 'Premium',
      storage: 'Unlimited',
      highlighted: true,
      description: [
        'Tout ce qui est inclus dans Standard',
        'Telechargement de la session pour la revoir a tout moment',
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
}
