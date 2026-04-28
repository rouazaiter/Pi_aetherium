package com.education.plateform.dto;

public record CheckoutSessionResponse(
        String sessionId,
        String checkoutUrl
) {
}