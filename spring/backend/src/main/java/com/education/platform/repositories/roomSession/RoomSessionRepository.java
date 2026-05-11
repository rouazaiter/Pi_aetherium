package com.education.platform.repositories.roomSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.education.platform.entities.roomSession.RoomSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomSessionRepository extends JpaRepository<RoomSession, Long> {
    List<RoomSession> findByStatus(RoomSession.SessionStatus status);
    List<RoomSession> findByHostUserId(Long hostUserId);
    List<RoomSession> findByStatusAndHostUserId(RoomSession.SessionStatus status, Long hostUserId);
    Optional<RoomSession> findFirstByStatusAndNameIgnoreCase(RoomSession.SessionStatus status, String name);
}