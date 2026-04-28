package com.education.platform.repositories;

import com.education.platform.entities.DirectConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DirectConversationRepository extends JpaRepository<DirectConversation, Long> {

    Optional<DirectConversation> findByUserA_IdAndUserB_Id(Long userAId, Long userBId);

    List<DirectConversation> findByUserA_IdOrUserB_Id(Long userAId, Long userBId);
}
