package com.education.platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.education.platform.models.SessionRecording;

import java.util.List;

@Repository
public interface SessionRecordingRepository extends JpaRepository<SessionRecording, Long> {
    List<SessionRecording> findBySessionId(Long sessionId);
    List<SessionRecording> findBySessionIdAndType(Long sessionId, SessionRecording.RecordingType type);
}