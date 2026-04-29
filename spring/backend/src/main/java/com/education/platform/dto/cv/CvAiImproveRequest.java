package com.education.platform.dto.cv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CvAiImproveRequest {

    @NotNull
    private CvAiImproveTopic topic;

    @NotNull
    private CvAiImproveSectionType sectionType;

    @Size(max = 100)
    private String field;

    @NotBlank
    @Size(max = 10000)
    private String text;

    @NotNull
    private CvAiImproveTargetTone targetTone;

    @NotNull
    private CvAiImproveMaxLength maxLength;

    @Valid
    private Context context;

    @Getter
    @Setter
    public static class Context {
        @Valid
        private PortfolioContext portfolio;
        @Valid
        private ProfileContext profile;
        @Valid
        private ProjectContext project;
        @Valid
        private ExperienceContext experience;
        @Valid
        private EducationContext education;
    }

    @Getter
    @Setter
    public static class PortfolioContext {
        @Size(max = 255)
        private String job;
        @Size(max = 3000)
        private String bio;
        @Size(max = 100)
        private List<@Size(max = 100) String> skills = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class ProfileContext {
        @Size(max = 255)
        private String fullName;
        @Size(max = 255)
        private String location;
    }

    @Getter
    @Setter
    public static class ProjectContext {
        @Size(max = 255)
        private String title;
        @Size(max = 5000)
        private String description;
        @Size(max = 100)
        private List<@Size(max = 100) String> skills = new ArrayList<>();
        @Size(max = 1000)
        private String projectUrl;
    }

    @Getter
    @Setter
    public static class ExperienceContext {
        @Size(max = 255)
        private String role;
        @Size(max = 255)
        private String company;
        @Size(max = 5000)
        private String summary;
    }

    @Getter
    @Setter
    public static class EducationContext {
        @Size(max = 255)
        private String degree;
        @Size(max = 255)
        private String school;
        @Size(max = 255)
        private String fieldOfStudy;
    }
}
