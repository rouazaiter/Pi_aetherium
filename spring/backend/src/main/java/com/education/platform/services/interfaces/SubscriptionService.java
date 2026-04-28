package com.education.platform.services.interfaces;

import com.education.platform.dto.subscription.SubscriptionRequest;
import com.education.platform.dto.subscription.SubscriptionResponse;
import com.education.platform.entities.User;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse createForUser(User user, SubscriptionRequest request);

    List<SubscriptionResponse> listForUser(User user);
}
