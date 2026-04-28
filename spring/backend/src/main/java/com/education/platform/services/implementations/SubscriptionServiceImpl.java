package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.subscription.SubscriptionRequest;
import com.education.platform.dto.subscription.SubscriptionResponse;
import com.education.platform.entities.Subscription;
import com.education.platform.entities.User;
import com.education.platform.repositories.SubscriptionRepository;
import com.education.platform.services.interfaces.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public SubscriptionResponse createForUser(User user, SubscriptionRequest request) {
        LocalDate start = request.getDateOfSubscription() != null ? request.getDateOfSubscription() : LocalDate.now();
        LocalDate billing = request.getBillingDate() != null ? request.getBillingDate() : start;
        if (request.getExpirationDate().isBefore(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La date d'expiration doit être après le début de l'abonnement");
        }
        Subscription sub = Subscription.builder()
                .user(user)
                .dateOfSubscription(start)
                .subscriptionPlan(request.getSubscriptionPlan())
                .expirationDate(request.getExpirationDate())
                .billingDate(billing)
                .build();
        subscriptionRepository.save(sub);
        return toResponse(sub);
    }

    @Override
    public List<SubscriptionResponse> listForUser(User user) {
        return subscriptionRepository.findByUser_IdOrderByDateOfSubscriptionDesc(user.getId()).stream()
                .map(SubscriptionServiceImpl::toResponse)
                .toList();
    }

    private static SubscriptionResponse toResponse(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .dateOfSubscription(s.getDateOfSubscription())
                .subscriptionPlan(s.getSubscriptionPlan())
                .expirationDate(s.getExpirationDate())
                .billingDate(s.getBillingDate())
                .build();
    }
}
