package com.education.platform.dto.explore;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExploreCollectionMiniResponse {

    private Long id;
    private String name;
    private String description;
    private Visibility visibility;
    private Integer projectCount;
    private String coverMediaUrl;
}
