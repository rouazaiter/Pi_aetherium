package com.education.platform.dto.portfolio;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioOwnerDto {

    private Long id;
    private String username;
    private String email;
}
