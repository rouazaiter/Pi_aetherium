package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.education.platform.entities.certifications.Certification;
import com.education.platform.repositories.certifications.CertificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.api.key:}")
    private String stripeSecretKey;

    private final CertificationRepository certificationRepository;
    private final EnrollmentService enrollmentService;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    public Map<String, String> createPaymentIntent(Long certId, String userIdentifier) throws StripeException {
        System.out.println("Creating payment intent for cert: " + certId + ", user: " + userIdentifier);
        
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        // Amount in cents
        long amount = cert.getPrice().multiply(new java.math.BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("usd")
                .putMetadata("certificationId", certId.toString())
                .putMetadata("userIdentifier", userIdentifier)
                .addPaymentMethodType("card") // Restrict to card only
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        return response;
    }
}
