package tn.esprit.backend.dto;

public record CheckoutSessionResponse(
        String sessionId,
        String checkoutUrl
) {
}