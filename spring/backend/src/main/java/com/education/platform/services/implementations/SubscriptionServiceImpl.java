package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.subscription.SubscriptionRequest;
import com.education.platform.dto.subscription.SubscriptionPlanResponse;
import com.education.platform.dto.subscription.SubscriptionResponse;
import com.education.platform.entities.SubscriptionPlan;
import com.education.platform.entities.SubscriptionStatus;
import com.education.platform.entities.Subscription;
import com.education.platform.entities.User;
import com.education.platform.repositories.SubscriptionRepository;
import com.education.platform.services.interfaces.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Map<SubscriptionPlan, PlanDefinition> PLAN_DEFINITIONS = Map.of(
            SubscriptionPlan.STANDARD, new PlanDefinition(
                    24.90,
                    30,
                    0,
                    List.of(
                            "Session securisee avec enregistrement en cas de probleme",
                            "Exercices pratiques pour mieux comprendre",
                            "Supervision apres la session et contact avec un expert"
                    )
            ),
            SubscriptionPlan.PREMIUM, new PlanDefinition(
                    49.90,
                    30,
                    0,
                    List.of(
                            "Tout ce qui est inclus dans Standard",
                            "Telechargement de la session pour la revoir a tout moment"
                    )
            )
    );

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<SubscriptionPlanResponse> listAvailablePlans() {
        return PLAN_DEFINITIONS.entrySet().stream()
                .sorted((a, b) -> Integer.compare(a.getKey().ordinal(), b.getKey().ordinal()))
                .map(entry -> SubscriptionPlanResponse.builder()
                        .plan(entry.getKey())
                        .monthlyPrice(entry.getValue().monthlyPrice())
                        .durationDays(entry.getValue().durationDays())
                        .trialDays(entry.getValue().trialDays())
                        .features(entry.getValue().features())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse createForUser(User user, SubscriptionRequest request) {
        LocalDate start = request.getDateOfSubscription() != null ? request.getDateOfSubscription() : LocalDate.now();
        LocalDate billing = request.getBillingDate() != null ? request.getBillingDate() : start;
        LocalDate expiration = request.getExpirationDate() != null
                ? request.getExpirationDate()
                : start.plusDays(resolvePlanDefinition(request.getSubscriptionPlan()).durationDays());
        if (expiration.isBefore(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La date d'expiration doit être après le début de l'abonnement");
        }

        subscriptionRepository.findFirstByUser_IdAndStatusOrderByDateOfSubscriptionDesc(user.getId(), SubscriptionStatus.ACTIVE)
                .ifPresent(previous -> {
                    previous.setStatus(SubscriptionStatus.CANCELLED);
                    previous.setAutoRenew(false);
                });

        Subscription sub = Subscription.builder()
                .user(user)
                .dateOfSubscription(start)
                .subscriptionPlan(request.getSubscriptionPlan())
                .status(SubscriptionStatus.ACTIVE)
                .expirationDate(expiration)
                .billingDate(billing)
                .autoRenew(request.getAutoRenew() == null || request.getAutoRenew())
                .build();
        subscriptionRepository.save(sub);
        return toResponse(sub);
    }

    @Override
    public List<SubscriptionResponse> listForUser(User user) {
        return subscriptionRepository.findByUser_IdOrderByDateOfSubscriptionDesc(user.getId()).stream()
                .map(this::markAsExpiredIfNeeded)
                .map(SubscriptionServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse getCurrentForUser(User user) {
        Subscription subscription = subscriptionRepository
                .findFirstByUser_IdAndStatusOrderByDateOfSubscriptionDesc(user.getId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aucun abonnement actif"));
        subscription = markAsExpiredIfNeeded(subscription);
        if (effectiveStatus(subscription) != SubscriptionStatus.ACTIVE) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Aucun abonnement actif");
        }
        return toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelForUser(User user, Long subscriptionId) {
        Subscription subscription = findOwnedSubscription(user, subscriptionId);
        if (effectiveStatus(subscription) == SubscriptionStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cet abonnement est deja annule");
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        return toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse updateAutoRenew(User user, Long subscriptionId, boolean autoRenew) {
        Subscription subscription = findOwnedSubscription(user, subscriptionId);
        if (effectiveStatus(subscription) != SubscriptionStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Seul un abonnement actif peut etre modifie");
        }
        subscription.setAutoRenew(autoRenew);
        return toResponse(subscription);
    }

    private Subscription findOwnedSubscription(User user, Long subscriptionId) {
        return subscriptionRepository.findByIdAndUser_Id(subscriptionId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Abonnement introuvable"));
    }

    private Subscription markAsExpiredIfNeeded(Subscription subscription) {
        if (effectiveStatus(subscription) == SubscriptionStatus.ACTIVE && subscription.getExpirationDate().isBefore(LocalDate.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setAutoRenew(false);
        }
        return subscription;
    }

    private SubscriptionStatus effectiveStatus(Subscription subscription) {
        return subscription.getStatus() == null ? SubscriptionStatus.ACTIVE : subscription.getStatus();
    }

    private PlanDefinition resolvePlanDefinition(SubscriptionPlan plan) {
        PlanDefinition definition = PLAN_DEFINITIONS.get(plan);
        if (definition == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Plan d'abonnement non supporte");
        }
        return definition;
    }

    private static SubscriptionResponse toResponse(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .dateOfSubscription(s.getDateOfSubscription())
                .subscriptionPlan(s.getSubscriptionPlan())
                .status(s.getStatus() == null ? SubscriptionStatus.ACTIVE : s.getStatus())
                .expirationDate(s.getExpirationDate())
                .billingDate(s.getBillingDate())
                .autoRenew(Boolean.TRUE.equals(s.getAutoRenew()))
                .build();
    }

    private record PlanDefinition(double monthlyPrice, int durationDays, int trialDays, List<String> features) {
    }
}
