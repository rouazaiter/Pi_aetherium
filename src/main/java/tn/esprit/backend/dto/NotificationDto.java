package tn.esprit.backend.dto;

import java.time.LocalDateTime;

public record NotificationDto(
  String type,
  String message,
  Long serviceRequestId,
  Long applicationId,
  LocalDateTime createdAt
) {}