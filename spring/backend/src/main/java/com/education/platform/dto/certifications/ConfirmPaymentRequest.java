package com.education.platform.dto.certifications;

public record ConfirmPaymentRequest(String userIdentifier, String paymentIntentId, String fullName, String phoneNumber) {}
