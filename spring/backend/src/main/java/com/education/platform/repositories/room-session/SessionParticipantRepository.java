package com.education.platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.education.platform.models.SessionParticipant;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
    List<SessionParticipant> findBySessionId(Long sessionId);
    Optional<SessionParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);
    Optional<SessionParticipant> findBySessionIdAndUserIdAndLeftAtIsNull(Long sessionId, Long userId);
    List<SessionParticipant> findBySessionIdAndLeftAtIsNull(Long sessionId);
    boolean existsBySessionIdAndUserIdAndLeftAtIsNull(Long sessionId, Long userId);
    boolean existsBySessionIdAndUserIdAndRoleAndLeftAtIsNull(Long sessionId, Long userId, SessionParticipant.ParticipantRole role);
}