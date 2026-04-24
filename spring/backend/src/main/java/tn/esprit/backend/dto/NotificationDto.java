package tn.esprit.backend.dto;

import java.time.LocalDateTime;

public record NotificationDto(
  String type,
  String message,
  NotificationPriority priority,
  String suggestedAction,
  boolean generatedByAi,
  Long serviceRequestId,
  Long applicationId,
  LocalDateTime createdAt
) {}