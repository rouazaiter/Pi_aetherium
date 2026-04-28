package com.education.plateform.services.interfaces;

import com.stripe.model.checkout.Session;
import com.education.plateform.dto.CheckoutSessionResponse;

public interface PaymentService {
    CheckoutSessionResponse createCheckoutSession(Long serviceRequestId, Long requesterId);
    void markAsPaidFromWebhook(Long serviceRequestId, Session session);
    CheckoutSessionResponse createCheckoutSessionForApplication(Long applicationId, Long requesterId);
    void markApplicationAsPaidFromWebhook(Long applicationId, Session session);
    void processMtnWebhook(java.util.Map<String, Object> payload);
}