package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.backend.dto.NotificationDto;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final SimpMessagingTemplate messagingTemplate;

  public void notifyUser(Long userId, NotificationDto dto) {
    messagingTemplate.convertAndSend("/topic/notifications/user/" + userId, dto);
  }
}