package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.backend.dto.NotificationDto;
import tn.esprit.backend.dto.NotificationPriority;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final SimpMessagingTemplate messagingTemplate;
  private final NotificationAiAssistantService notificationAiAssistantService;

  public void notifyUser(Long userId, NotificationDto dto) {
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
}