package com.education.plateform.controllers;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.education.plateform.dto.CheckoutSessionResponse;
import com.education.plateform.services.interfaces.PaymentService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @PostMapping("/checkout/{serviceRequestId}/{requester-id}")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @PathVariable Long serviceRequestId,
            @PathVariable("requester-id") Long requesterId
    ) {
        return ResponseEntity.ok(paymentService.createCheckoutSession(serviceRequestId, requesterId));
    }

    @PostMapping("/checkout-application/{applicationId}/{requester-id}")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSessionForApplication(
            @PathVariable Long applicationId,
            @PathVariable("requester-id") Long requesterId
    ) {
        return ResponseEntity.ok(paymentService.createCheckoutSessionForApplication(applicationId, requesterId));
    }

    @PostMapping("/mtn-webhook")
    public ResponseEntity<String> handleMtnWebhook(@RequestBody Map<String, Object> payload) {
        paymentService.processMtnWebhook(payload);
        return ResponseEntity.ok("received");
    }

    @GetMapping("/mtn-webhook/sample")
    public ResponseEntity<Map<String, Object>> sampleMtnWebhook() {
        Map<String, Object> sample = new LinkedHashMap<>();
        sample.put("transactionId", "MTN123456789");
        sample.put("status", "SUCCESS");
        sample.put("amount", "100.00");
        sample.put("currency", "XAF");
        sample.put("reference", "REF123");
        sample.put("msisdn", "+237699000000");
        sample.put("payerName", "Jean Dupont");
        sample.put("message", "Paiement MTN généré pour test");
        return ResponseEntity.ok(sample);
    }

    @PostMapping("/mtn-webhook/generate")
    public ResponseEntity<Map<String, Object>> generateMtnWebhook(@RequestBody(required = false) Map<String, Object> customPayload) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("transactionId", "MTN123456789");
        payload.put("status", "SUCCESS");
        payload.put("amount", "100.00");
        payload.put("currency", "XAF");
        payload.put("reference", "REF123");
        payload.put("msisdn", "+237699000000");
        payload.put("payerName", "Jean Dupont");
        payload.put("message", "Paiement MTN généré pour test");

        if (customPayload != null) {
            payload.putAll(customPayload);
        }

        return ResponseEntity.ok(payload);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid checkout session payload"));

                // Check if this is for a ServiceRequest or Application
                String serviceRequestId = session.getMetadata().get("serviceRequestId");
                String applicationId = session.getMetadata().get("applicationId");

                if (serviceRequestId != null && !serviceRequestId.isBlank()) {
                    paymentService.markAsPaidFromWebhook(Long.valueOf(serviceRequestId), session);
                } else if (applicationId != null && !applicationId.isBlank()) {
                    paymentService.markApplicationAsPaidFromWebhook(Long.valueOf(applicationId), session);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing serviceRequestId or applicationId metadata");
                }
            }

            return ResponseEntity.ok("ok");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe webhook", ex);
        }
    }
}