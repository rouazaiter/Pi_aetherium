package com.education.platform.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendSearchResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String relation;
}
