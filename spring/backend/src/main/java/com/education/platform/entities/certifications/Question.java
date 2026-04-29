package com.education.platform.entities.certifications;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private QuestionType type;

    @Column(length = 2000)
    private String questionText;

    /** For MATCH / DRAG_DROP / ORDERING: stores pairs or ordered items as JSON */
    @Column(length = 4000)
    private String matchItems;

    /** For MCQ / MULTI_SELECT: JSON array of option strings e.g. ["A","B","C","D"] */
    @Column(length = 4000)
    private String options;

    /** Correct answer(s):
     *  - MCQ: single letter/index  e.g. "B"
     *  - MULTI_SELECT: comma-separated e.g. "A,C"
     *  - FILL_BLANK / SCENARIO: expected text
     *  - ORDERING: comma-separated correct order indices e.g. "2,0,3,1"
     */
    @Column(length = 2000)
    private String expectedAnswer;

    /** For CODE questions */
    private String codeLanguage;

    private Double points;
    private Integer orderIndex;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Choice> choices;

    public enum QuestionType {
        // ── 40-50% ──────────────────────────────────────────────────────────
        MCQ,           // Single correct answer from 4 options
        MULTI_SELECT,  // Multiple correct answers (select all that apply)

        // ── 20% ─────────────────────────────────────────────────────────────
        SCENARIO,      // Real-world situation, pick best approach

        // ── 20% ─────────────────────────────────────────────────────────────
        CODE,          // Code output prediction / debugging / logic tracing

        // ── 10-20% ──────────────────────────────────────────────────────────
        ORDERING,      // Arrange steps in correct sequence
        DRAG_DROP,     // Match / drag items to correct targets
        MATCH,         // Classic left↔right matching

        // ── Legacy (kept for backward compat) ───────────────────────────────
        FILL_BLANK,
        EXPLAIN,
        WRITE
    }
}
