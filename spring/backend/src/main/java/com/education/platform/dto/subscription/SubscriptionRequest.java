package com.education.platform.dto.subscription;

import com.education.platform.entities.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionRequest {

    @NotNull(message = "Plan requis")
    private SubscriptionPlan subscriptionPlan;

    private LocalDate dateOfSubscription;

    private LocalDate expirationDate;

    private LocalDate billingDate;

    private Boolean autoRenew;
}
