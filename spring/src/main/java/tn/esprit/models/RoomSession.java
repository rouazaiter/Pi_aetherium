package tn.esprit.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_sessions")
public class RoomSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "agora_channel_name")
    private String agoraChannelName;

    @Column(name = "agora_token")
    private String agoraToken;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionRecording> recordings = new ArrayList<>();

    public enum SessionStatus {
        ACTIVE, ENDED
    }

    public RoomSession() {
    }

    public RoomSession(String name, Long hostUserId) {
        this.name = name;
        this.hostUserId = hostUserId;
        this.status = SessionStatus.ACTIVE;
        this.startTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getAgoraChannelName() {
        return agoraChannelName;
    }

    public void setAgoraChannelName(String agoraChannelName) {
        this.agoraChannelName = agoraChannelName;
    }

    public String getAgoraToken() {
        return agoraToken;
    }

    public void setAgoraToken(String agoraToken) {
        this.agoraToken = agoraToken;
    }

    public List<SessionParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<SessionParticipant> participants) {
        this.participants = participants;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<SessionRecording> getRecordings() {
        return recordings;
    }

    public void setRecordings(List<SessionRecording> recordings) {
        this.recordings = recordings;
    }
}
