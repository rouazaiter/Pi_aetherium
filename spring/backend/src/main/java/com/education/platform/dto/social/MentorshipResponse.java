package com.education.platform.dto.social;

import com.education.platform.entities.MentorshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorshipResponse {

    private Long id;
    private Long mentorId;
    private String mentorUsername;
    private Long menteeId;
    private String menteeUsername;
    private MentorshipStatus status;
    private Instant requestedAt;
    private Instant acceptedAt;
}
