package com.education.platform.dto.certifications;

public record PaymentIntentResponse(String clientSecret, String paymentIntentId, double amount) {}
