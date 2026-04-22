package com.education.platform.controllers;

import com.education.platform.dto.subscription.SubscriptionRequest;
import com.education.platform.dto.subscription.SubscriptionPlanResponse;
import com.education.platform.dto.subscription.SubscriptionResponse;
import com.education.platform.dto.subscription.UpdateAutoRenewRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final CurrentUserService currentUserService;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(CurrentUserService currentUserService, SubscriptionService subscriptionService) {
        this.currentUserService = currentUserService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/plans")
    public List<SubscriptionPlanResponse> plans() {
        return subscriptionService.listAvailablePlans();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.createForUser(currentUserService.getCurrentUser(), request);
    }

    @GetMapping("/me")
    public List<SubscriptionResponse> mySubscriptions() {
        return subscriptionService.listForUser(currentUserService.getCurrentUser());
    }

    @GetMapping("/me/current")
    public SubscriptionResponse myCurrentSubscription() {
        return subscriptionService.getCurrentForUser(currentUserService.getCurrentUser());
    }

    @PatchMapping("/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable Long id) {
        return subscriptionService.cancelForUser(currentUserService.getCurrentUser(), id);
    }

    @PatchMapping("/{id}/auto-renew")
    public SubscriptionResponse updateAutoRenew(@PathVariable Long id, @Valid @RequestBody UpdateAutoRenewRequest request) {
        return subscriptionService.updateAutoRenew(currentUserService.getCurrentUser(), id, request.getAutoRenew());
    }
}
