package com.education.platform.services.interfaces;

import com.education.platform.dto.subscription.SubscriptionRequest;
import com.education.platform.dto.subscription.SubscriptionPlanResponse;
import com.education.platform.dto.subscription.SubscriptionResponse;
import com.education.platform.entities.User;

import java.util.List;

public interface SubscriptionService {

    List<SubscriptionPlanResponse> listAvailablePlans();

    SubscriptionResponse createForUser(User user, SubscriptionRequest request);

    List<SubscriptionResponse> listForUser(User user);

    SubscriptionResponse getCurrentForUser(User user);

    SubscriptionResponse cancelForUser(User user, Long subscriptionId);

    SubscriptionResponse updateAutoRenew(User user, Long subscriptionId, boolean autoRenew);
}
