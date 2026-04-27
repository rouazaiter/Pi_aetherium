package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePortfolioCollectionRequest {

    private String name;
    private String description;
    private Visibility visibility;
}
