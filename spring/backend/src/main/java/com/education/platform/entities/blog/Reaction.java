package com.education.platform.entities.blog;

import jakarta.persistence.*;
import com.education.platform.entities.user.User;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "message_id"})
    }
)
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private DiscussionMessage message;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReactionType getType() { return type; }
    public void setType(ReactionType type) { this.type = type; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public DiscussionMessage getMessage() { return message; }
    public void setMessage(DiscussionMessage message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
