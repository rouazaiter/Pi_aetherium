package tn.esprit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.models.SessionParticipant;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
    List<SessionParticipant> findBySessionId(Long sessionId);
    Optional<SessionParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);
    List<SessionParticipant> findBySessionIdAndLeftAtIsNull(Long sessionId);
    boolean existsBySessionIdAndUserIdAndLeftAtIsNull(Long sessionId, Long userId);
}