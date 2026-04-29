package com.education.platform.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.education.platform.models.*;
import com.education.platform.repositories.*;
import com.education.platform.services.interfaces.IRoomSessionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomSessionService implements IRoomSessionService {

    private final RoomSessionRepository roomSessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final SessionRecordingRepository recordingRepository;

    public RoomSessionService(
            RoomSessionRepository roomSessionRepository,
            SessionParticipantRepository participantRepository,
            ChatMessageRepository messageRepository,
            SessionRecordingRepository recordingRepository) {
        this.roomSessionRepository = roomSessionRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.recordingRepository = recordingRepository;
    }

    @Override
    @Transactional
    public RoomSession createRoom(String name, Long hostUserId) {
        RoomSession room = new RoomSession(name, hostUserId);
        room.setAgoraChannelName("room-" + UUID.randomUUID().toString().substring(0, 8));
        room = roomSessionRepository.save(room);

        SessionParticipant host = new SessionParticipant(
                room, hostUserId, "Host", SessionParticipant.ParticipantRole.HOST);
        participantRepository.save(host);

        return room;
    }

    @Override
    public Optional<RoomSession> getRoom(Long roomId) {
        return roomSessionRepository.findById(roomId);
    }

    @Override
    public List<RoomSession> getActiveRooms() {
        return roomSessionRepository.findByStatus(RoomSession.SessionStatus.ACTIVE);
    }

    @Override
    public Optional<RoomSession> getActiveRoomByName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            return Optional.empty();
        }
        return roomSessionRepository.findFirstByStatusAndNameIgnoreCase(
                RoomSession.SessionStatus.ACTIVE,
                roomName.trim()
        );
    }

    @Override
    @Transactional
    public RoomSession setWorkspaceAccessBlocked(Long roomId, Long actorUserId, boolean blocked) {
        RoomSession room = roomSessionRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        boolean isHostParticipant = participantRepository.existsBySessionIdAndUserIdAndRoleAndLeftAtIsNull(
                roomId,
                actorUserId,
                SessionParticipant.ParticipantRole.HOST
        );
        if (!isHostParticipant) {
            throw new RuntimeException("Only HOST can change workspace access");
        }
        room.setWorkspaceAccessBlocked(blocked);
        return roomSessionRepository.save(room);
    }

    @Override
    @Transactional
    public SessionParticipant joinRoom(Long roomId, Long userId, String userName) {
        RoomSession room = roomSessionRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus() != RoomSession.SessionStatus.ACTIVE) {
            throw new RuntimeException("Room is no longer active");
        }

        Optional<SessionParticipant> existingActiveParticipant =
                participantRepository.findBySessionIdAndUserIdAndLeftAtIsNull(roomId, userId);
        if (existingActiveParticipant.isPresent()) {
            return existingActiveParticipant.get();
        }

        SessionParticipant participant = new SessionParticipant(
                room, userId, userName, SessionParticipant.ParticipantRole.PARTICIPANT);
        return participantRepository.save(participant);
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        SessionParticipant participant = participantRepository
                .findBySessionIdAndUserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

    @Override
    @Transactional
    public RoomSession endRoom(Long roomId, Long userId) {
        RoomSession room = roomSessionRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getHostUserId().equals(userId)) {
            throw new RuntimeException("Only host can end the room");
        }

        room.setStatus(RoomSession.SessionStatus.ENDED);
        room.setEndTime(LocalDateTime.now());
        return roomSessionRepository.save(room);
    }

    @Override
    public List<SessionParticipant> getParticipants(Long roomId) {
        return participantRepository.findBySessionId(roomId);
    }

    @Override
    public List<SessionParticipant> getActiveParticipants(Long roomId) {
        return participantRepository.findBySessionIdAndLeftAtIsNull(roomId);
    }

    @Override
    public ChatMessage addMessage(Long roomId, Long senderId, String senderName, String content) {
        RoomSession room = roomSessionRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatMessage message = new ChatMessage(room, senderId, senderName, content);
        return messageRepository.save(message);
    }

    @Override
    public List<ChatMessage> getMessages(Long roomId) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(roomId);
    }

    @Override
    @Transactional
    public SessionRecording addRecording(Long roomId, String fileName, String filePath,
                                    Long fileSize, String contentType, SessionRecording.RecordingType type) {
        RoomSession room = roomSessionRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        SessionRecording recording = new SessionRecording(room, fileName, filePath, type);
        recording.setFileSize(fileSize);
        recording.setContentType(contentType);
        return recordingRepository.save(recording);
    }

    @Override
    public List<SessionRecording> getRecordings(Long roomId) {
        return recordingRepository.findBySessionId(roomId);
    }

    @Override
    public Optional<SessionRecording> getRecording(Long recordingId) {
        return recordingRepository.findById(recordingId);
    }
}