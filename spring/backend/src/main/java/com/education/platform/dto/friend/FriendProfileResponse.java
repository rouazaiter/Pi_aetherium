package com.education.platform.dto.friend;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class FriendProfileResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String description;
    private List<String> interests;
    private String profilePicture;
    private Boolean activeNow;
    private Instant lastActiveAt;
}
