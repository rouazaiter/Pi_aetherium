package com.education.platform.dto.subscription;

import com.education.platform.entities.SubscriptionPlan;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SubscriptionPlanResponse {

    private SubscriptionPlan plan;
    private double monthlyPrice;
    private int durationDays;
    private int trialDays;
    private List<String> features;
}
