package com.education.platform.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long fileId;
    
    private String fileName;

    private int score;

    private int totalQuestions;

    private LocalDateTime completedAt;
}
