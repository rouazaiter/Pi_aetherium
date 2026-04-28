package com.education.platform.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserOptionResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
}
