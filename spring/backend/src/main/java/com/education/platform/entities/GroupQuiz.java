package com.education.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "GroupQuiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "option_a", nullable = false, length = 200)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 200)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 200)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 200)
    private String optionD;

    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
