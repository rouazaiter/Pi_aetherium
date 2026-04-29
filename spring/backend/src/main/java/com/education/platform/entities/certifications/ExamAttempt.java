package com.education.platform.entities.certifications;

import jakarta.persistence.*;
import lombok.*;

/** Stores a user's answer to a single question */
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(length = 3000)
    private String userAnswer;
}
