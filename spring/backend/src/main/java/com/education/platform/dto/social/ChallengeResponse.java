package com.education.platform.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeResponse {

    private Long id;
    private Long groupId;
    private String title;
    private String description;
    private String topic;
    private int targetValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private Instant createdAt;
    private Integer myProgress;
    private Integer myPoints;
    private boolean myCompleted;
}
