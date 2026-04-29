package com.education.platform.entities.blog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.education.platform.entities.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "discussions")
public class Discussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscussionStatus status = DiscussionStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
            name = "discussion_participants",
            joinColumns = @JoinColumn(name = "discussion_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DiscussionMessage> messages = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private Long solvedMessageId; // ID of the accepted answer message

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public DiscussionStatus getStatus() { return status; }
    public void setStatus(DiscussionStatus status) { this.status = status; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public Set<User> getParticipants() { return participants; }
    public void setParticipants(Set<User> participants) { this.participants = participants; }
    public List<DiscussionMessage> getMessages() { return messages; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getSolvedMessageId() { return solvedMessageId; }
    public void setSolvedMessageId(Long solvedMessageId) { this.solvedMessageId = solvedMessageId; }
}
