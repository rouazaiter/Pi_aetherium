package com.education.platform.dto.friend;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private Boolean activeNow;
    private Instant lastActiveAt;
}
