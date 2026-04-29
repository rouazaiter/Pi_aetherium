package com.education.platform.entities.certifications;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    // For MATCH: left side label
    private String matchLeft;

    // For MATCH: right side label (the correct match)
    private String matchRight;

    // Generic text (used for FILL_BLANK hints, etc.)
    private String text;

    private boolean isCorrect;
}