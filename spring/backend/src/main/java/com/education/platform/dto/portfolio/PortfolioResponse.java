package com.education.platform.dto.portfolio;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortfolioResponse {

    private PortfolioDataDto portfolio;
    private PortfolioOwnerDto owner;
    private PortfolioProfileDto profile;
    private List<PortfolioProjectResponse> projects;
    private List<PortfolioCollectionResponse> collections;
}
