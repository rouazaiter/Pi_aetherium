package com.education.platform.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupMemberResponse {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String role;
    private Integer score;
}
