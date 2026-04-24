package tn.esprit.backend.controllers;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.CheckoutSessionResponse;
import tn.esprit.backend.services.interfaces.PaymentService;

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