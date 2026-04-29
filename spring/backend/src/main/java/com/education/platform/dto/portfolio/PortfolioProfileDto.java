package com.education.platform.dto.portfolio;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortfolioProfileDto {

    private String firstName;
    private String lastName;
    private String profilePicture;
    private String location;
    private List<String> interests;
}
