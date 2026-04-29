package com.education.platform.services.interfaces;

import com.education.platform.models.ChatMessage;
import com.education.platform.models.RoomSession;
import com.education.platform.models.SessionParticipant;
import com.education.platform.models.SessionRecording;

import java.util.List;
import java.util.Optional;

public interface IRoomSessionService {
    RoomSession createRoom(String name, Long hostUserId);

    Optional<RoomSession> getRoom(Long roomId);

    List<RoomSession> getActiveRooms();

    SessionParticipant joinRoom(Long roomId, Long userId, String userName);

    void leaveRoom(Long roomId, Long userId);

    RoomSession endRoom(Long roomId, Long userId);

    List<SessionParticipant> getParticipants(Long roomId);

    List<SessionParticipant> getActiveParticipants(Long roomId);

    ChatMessage addMessage(Long roomId, Long senderId, String senderName, String content);

    List<ChatMessage> getMessages(Long roomId);

    SessionRecording addRecording(Long roomId, String fileName, String filePath,
                                  Long fileSize, String contentType, SessionRecording.RecordingType type);

    List<SessionRecording> getRecordings(Long roomId);

    Optional<SessionRecording> getRecording(Long recordingId);
}
