package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdatePortfolioProjectRequest {

    private String title;
    private String description;
    private String projectUrl;
    private Boolean pinned;
    private Visibility visibility;
    private List<Long> skillIds;
    private List<ProjectMediaRequest> media;
}
