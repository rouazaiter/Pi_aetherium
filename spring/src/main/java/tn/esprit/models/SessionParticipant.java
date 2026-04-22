package tn.esprit.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_participants")
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private RoomSession session;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    public enum ParticipantRole {
        HOST, PARTICIPANT
    }

    public SessionParticipant() {
    }

    public SessionParticipant(RoomSession session, Long userId, String userName, ParticipantRole role) {
        this.session = session;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoomSession getSession() {
        return session;
    }

    public void setSession(RoomSession session) {
        this.session = session;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }
}