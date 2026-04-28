package com.education.platform.dto.social;

import com.education.platform.entities.GoalStatus;
import com.education.platform.entities.GoalVisibility;
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
public class GoalResponse {

    private Long id;
    private String title;
    private String topic;
    private int targetValue;
    private int currentValue;
    private int completionPercent;
    private LocalDate deadline;
    private GoalVisibility visibility;
    private GoalStatus status;
    private String ownerUsername;
    private Long groupId;
    private Instant updatedAt;
}
