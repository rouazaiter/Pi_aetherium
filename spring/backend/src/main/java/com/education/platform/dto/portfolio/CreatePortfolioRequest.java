package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreatePortfolioRequest {

    private String title;
    private String bio;
    private String coverImage;
    private String job;
    private String githubUrl;
    private String linkedinUrl;
    private Boolean openToWork;
    private Boolean availableForFreelance;
    private Visibility visibility;
    private List<Long> skillIds = new ArrayList<>();
}
