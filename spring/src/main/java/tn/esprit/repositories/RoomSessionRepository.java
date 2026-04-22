package tn.esprit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.models.RoomSession;

import java.util.List;

@Repository
public interface RoomSessionRepository extends JpaRepository<RoomSession, Long> {
    List<RoomSession> findByStatus(RoomSession.SessionStatus status);
    List<RoomSession> findByHostUserId(Long hostUserId);
    List<RoomSession> findByStatusAndHostUserId(RoomSession.SessionStatus status, Long hostUserId);
}