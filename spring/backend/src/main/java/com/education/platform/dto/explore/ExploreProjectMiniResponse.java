package com.education.platform.dto.explore;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExploreProjectMiniResponse {

    private Long id;
    private String title;
    private String description;
    private String projectUrl;
    private String imageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private Visibility visibility;
    private Integer views;
    private Integer likes;
    private String coverMediaUrl;
    private List<ExploreSkillMiniDto> skills;
}
