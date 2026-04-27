package com.education.platform.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroupResponse {

    private Long id;
    private String name;
    private String description;
    private String topic;
    private String imageUrl;
    private Instant createdAt;
    private String ownerUsername;
    private int memberCount;
    private String myRole;
}
