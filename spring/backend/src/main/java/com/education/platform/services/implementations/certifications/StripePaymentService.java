package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripePaymentService {

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    /**
     * Creates a Stripe PaymentIntent for the given amount (in USD).
     * Returns the client_secret needed by the frontend to confirm payment.
     */
    public String createPaymentIntent(BigDecimal amount, String description) {
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            throw new RuntimeException("Stripe API key not configured");
        }

        Stripe.apiKey = stripeApiKey;

        try {
            // Convert dollars to cents
            long amountCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("usd")
                    .setDescription(description)
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                            .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();

        } catch (Exception e) {
            throw new RuntimeException("Stripe PaymentIntent creation failed: " + e.getMessage());
        }
    }

    /**
     * Verifies a PaymentIntent was successfully paid.
     */
    public boolean verifyPayment(String paymentIntentId) {
        Stripe.apiKey = stripeApiKey;
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return "succeeded".equals(intent.getStatus());
        } catch (Exception e) {
            return false;
        }
    }
}
