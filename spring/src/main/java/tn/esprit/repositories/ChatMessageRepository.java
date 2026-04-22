package tn.esprit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.models.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);
    List<ChatMessage> findBySessionIdAndTimestampAfterOrderByTimestampAsc(Long sessionId, java.time.LocalDateTime timestamp);
}