package tn.esprit.aetherium.skillhubbackend.entities.blog;

import jakarta.persistence.*;
import tn.esprit.aetherium.skillhubbackend.entities.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    public enum ReportReason {
        SPAM, INAPPROPRIATE, MISINFORMATION, HARASSMENT, COPYRIGHT, OTHER
    }

    public enum ReportStatus {
        PENDING, REVIEWED, DISMISSED, ACTION_TAKEN
    }

    public enum TargetType { POST, COMMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    private Long targetId; // postId or commentId

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason reason) { this.reason = reason; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
