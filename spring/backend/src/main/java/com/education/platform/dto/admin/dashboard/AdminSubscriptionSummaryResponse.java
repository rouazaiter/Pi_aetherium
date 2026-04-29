package com.education.platform.dto.admin.dashboard;

import com.education.platform.entities.SubscriptionPlan;
import com.education.platform.entities.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubscriptionSummaryResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus status;
    private LocalDate dateOfSubscription;
    private LocalDate expirationDate;
    private LocalDate billingDate;
    private boolean autoRenew;
}
