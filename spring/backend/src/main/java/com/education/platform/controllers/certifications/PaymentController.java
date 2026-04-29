package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.stripe.exception.StripeException;
import com.education.platform.services.implementations.certifications.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestBody Map<String, Object> data) throws StripeException {
        Long certId = Long.valueOf(data.get("certificationId").toString());
        String userIdentifier = data.get("userIdentifier").toString();
        return paymentService.createPaymentIntent(certId, userIdentifier);
    }
}
