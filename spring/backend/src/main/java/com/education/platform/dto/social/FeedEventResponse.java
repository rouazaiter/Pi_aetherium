package com.education.platform.dto.social;

import com.education.platform.entities.FeedEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedEventResponse {

    private Long id;
    private FeedEventType type;
    private String actorUsername;
    private String message;
    private String topic;
    private Long groupId;
    private Long goalId;
    private Long challengeId;
    private Instant createdAt;
    private double rankingScore;
}
