package tn.esprit.backend.services.interfaces;

import com.stripe.model.checkout.Session;
import tn.esprit.backend.dto.CheckoutSessionResponse;

public interface PaymentService {
    CheckoutSessionResponse createCheckoutSession(Long serviceRequestId, Long requesterId);
    void markAsPaidFromWebhook(Long serviceRequestId, Session session);
    CheckoutSessionResponse createCheckoutSessionForApplication(Long applicationId, Long requesterId);
    void markApplicationAsPaidFromWebhook(Long applicationId, Session session);
}