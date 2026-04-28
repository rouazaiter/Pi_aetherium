package com.education.platform.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AiJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;

    @Enumerated(EnumType.STRING)
    private JobStatus status;
    // PENDING, PROCESSING, DONE, ERROR

    @Lob
    private String result;

    private LocalDateTime createdAt;
}