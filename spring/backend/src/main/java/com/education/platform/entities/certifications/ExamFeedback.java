package com.education.platform.entities.certifications;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who submitted the feedback */
    private String userIdentifier;

    /** Link to the enrollment (attempt) */
    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    /** Link to the certification */
    @ManyToOne
    @JoinColumn(name = "certification_id")
    private Certification certification;

    /** Score at time of feedback */
    private Double score;

    /** EASY | BALANCED | HARD */
    @Enumerated(EnumType.STRING)
    private DifficultyRating difficultyRating;

    /** TOO_SHORT | ADEQUATE | TOO_LONG */
    @Enumerated(EnumType.STRING)
    private TimeRating timeRating;

    /** YES | PARTIALLY | NO */
    @Enumerated(EnumType.STRING)
    private RelevanceRating relevanceRating;

    /** Optional free-text comment */
    @Column(length = 2000)
    private String comment;

    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() { submittedAt = LocalDateTime.now(); }

    public enum DifficultyRating { EASY, BALANCED, HARD }
    public enum TimeRating       { TOO_SHORT, ADEQUATE, TOO_LONG }
    public enum RelevanceRating  { YES, PARTIALLY, NO }
}
