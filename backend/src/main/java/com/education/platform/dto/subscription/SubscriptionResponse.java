package com.education.platform.dto.subscription;

import com.education.platform.entities.SubscriptionPlan;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SubscriptionResponse {

    private Long id;
    private LocalDate dateOfSubscription;
    private SubscriptionPlan subscriptionPlan;
    private LocalDate expirationDate;
    private LocalDate billingDate;
}
