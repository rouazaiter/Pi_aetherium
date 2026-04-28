package com.education.platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.education.platform.models.RoomSession;

import java.util.List;

@Repository
public interface RoomSessionRepository extends JpaRepository<RoomSession, Long> {
    List<RoomSession> findByStatus(RoomSession.SessionStatus status);
    List<RoomSession> findByHostUserId(Long hostUserId);
    List<RoomSession> findByStatusAndHostUserId(RoomSession.SessionStatus status, Long hostUserId);
}