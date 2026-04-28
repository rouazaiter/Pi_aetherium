package com.education.platform.controllers;

import com.education.platform.dto.subscription.SubscriptionRequest;
import com.education.platform.dto.subscription.SubscriptionResponse;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.createForUser(currentUserService.getCurrentUser(), request);
    }

    @GetMapping("/me")
    public List<SubscriptionResponse> mySubscriptions() {
        return subscriptionService.listForUser(currentUserService.getCurrentUser());
    }
}
