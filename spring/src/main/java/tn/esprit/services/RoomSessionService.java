package tn.esprit.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.models.*;
import tn.esprit.repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomSessionService {

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

    public Optional<RoomSession> getRoom(Long roomId) {
        return roomSessionRepository.findById(roomId);
    }

    public List<RoomSession> getActiveRooms() {
        return roomSessionRepository.findByStatus(RoomSession.SessionStatus.ACTIVE);
    }

    @Transactional
    public SessionParticipant joinRoom(Long roomId, Long userId, String userName) {
        RoomSession room = roomSessionRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus() != RoomSession.SessionStatus.ACTIVE) {
            throw new RuntimeException("Room is no longer active");
        }

        if (participantRepository.existsBySessionIdAndUserIdAndLeftAtIsNull(roomId, userId)) {
            throw new RuntimeException("User already in room");
        }

        SessionParticipant participant = new SessionParticipant(
                room, userId, userName, SessionParticipant.ParticipantRole.PARTICIPANT);
        return participantRepository.save(participant);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        SessionParticipant participant = participantRepository
                .findBySessionIdAndUserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

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

    public List<SessionParticipant> getParticipants(Long roomId) {
        return participantRepository.findBySessionId(roomId);
    }

    public List<SessionParticipant> getActiveParticipants(Long roomId) {
        return participantRepository.findBySessionIdAndLeftAtIsNull(roomId);
    }

    public ChatMessage addMessage(Long roomId, Long senderId, String senderName, String content) {
        RoomSession room = roomSessionRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatMessage message = new ChatMessage(room, senderId, senderName, content);
        return messageRepository.save(message);
    }

    public List<ChatMessage> getMessages(Long roomId) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(roomId);
    }

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

    public List<SessionRecording> getRecordings(Long roomId) {
        return recordingRepository.findBySessionId(roomId);
    }

    public Optional<SessionRecording> getRecording(Long recordingId) {
        return recordingRepository.findById(recordingId);
    }
}