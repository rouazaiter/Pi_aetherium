package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.backend.dto.NotificationDto;
import tn.esprit.backend.dto.NotificationPriority;
import tn.esprit.backend.entities.UserNotification;
import tn.esprit.backend.repositories.UserNotificationRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final SimpMessagingTemplate messagingTemplate;
  private final NotificationAiAssistantService notificationAiAssistantService;
  private final UserNotificationRepository userNotificationRepository;

  public void notifyUser(Long userId, NotificationDto dto) {
    userNotificationRepository.save(UserNotification.builder()
            .userId(userId)
            .type(dto.type())
            .message(dto.message())
            .priority(dto.priority())
            .suggestedAction(dto.suggestedAction())
            .generatedByAi(dto.generatedByAi())
            .serviceRequestId(dto.serviceRequestId())
            .applicationId(dto.applicationId())
            .createdAt(dto.createdAt() == null ? LocalDateTime.now() : dto.createdAt())
            .build());

    messagingTemplate.convertAndSend("/topic/notifications/user/" + userId, dto);
  }

  public void notifyUserWithAssistant(
          Long userId,
          String type,
          String fallbackMessage,
          String actorName,
          String requestName,
          NotificationPriority fallbackPriority,
          String fallbackAction,
          Long serviceRequestId,
          Long applicationId
  ) {
    NotificationAiAssistantService.NotificationPayload payload = notificationAiAssistantService.build(
            type,
            fallbackMessage,
            actorName,
            requestName,
            fallbackPriority,
            fallbackAction
    );

    notifyUser(
            userId,
            new NotificationDto(
                    type,
                    payload.message(),
                    payload.priority(),
                    payload.suggestedAction(),
                    payload.generatedByAi(),
                    serviceRequestId,
                    applicationId,
                    LocalDateTime.now()
            )
    );
  }

  public void notifyUsersWithAssistant(
          List<Long> userIds,
          String type,
          String fallbackMessage,
          String actorName,
          String requestName,
          NotificationPriority fallbackPriority,
          String fallbackAction,
          Long serviceRequestId,
          Long applicationId
  ) {
    if (userIds == null || userIds.isEmpty()) {
      return;
    }
    for (Long userId : userIds) {
      notifyUserWithAssistant(
              userId,
              type,
              fallbackMessage,
              actorName,
              requestName,
              fallbackPriority,
              fallbackAction,
              serviceRequestId,
              applicationId
      );
    }
  }

  public List<NotificationDto> getRecentNotifications(Long userId, int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 50));

    return userNotificationRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
            .sorted(Comparator.comparing(UserNotification::getCreatedAt).reversed())
            .limit(safeLimit)
            .map(notification -> new NotificationDto(
                    notification.getType(),
                    notification.getMessage(),
                    notification.getPriority(),
                    notification.getSuggestedAction(),
                    notification.isGeneratedByAi(),
                    notification.getServiceRequestId(),
                    notification.getApplicationId(),
                    notification.getCreatedAt()
            ))
            .toList();
  }
}