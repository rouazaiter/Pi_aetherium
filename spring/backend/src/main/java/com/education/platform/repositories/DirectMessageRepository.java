package com.education.platform.repositories;

import com.education.platform.entities.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    List<DirectMessage> findByConversation_IdOrderBySentAtAsc(Long conversationId);

    DirectMessage findFirstByConversation_IdOrderBySentAtDesc(Long conversationId);

    long countByConversation_IdAndSender_IdNotAndReadFalse(Long conversationId, Long senderId);
}
