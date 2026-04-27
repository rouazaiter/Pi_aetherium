package com.education.platform.services.implementations.portfolio;

import com.education.platform.dto.portfolio.PortfolioCollectionResponse;
import com.education.platform.dto.portfolio.PortfolioDataDto;
import com.education.platform.dto.portfolio.PortfolioOwnerDto;
import com.education.platform.dto.portfolio.PortfolioProfileDto;
import com.education.platform.dto.portfolio.PortfolioProjectResponse;
import com.education.platform.dto.portfolio.PortfolioResponse;
import com.education.platform.dto.portfolio.ProjectMediaResponse;
import com.education.platform.dto.portfolio.ProjectSummaryDto;
import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.CollectionProject;
import com.education.platform.entities.portfolio.MediaType;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioCollection;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.ProjectMedia;
import com.education.platform.entities.portfolio.Skill;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class PortfolioMapper {

    private static final Comparator<Skill> SKILL_ORDER =
            Comparator.comparing(Skill::getName, String.CASE_INSENSITIVE_ORDER);

    private static final Comparator<ProjectMedia> MEDIA_ORDER =
            Comparator.comparing((ProjectMedia media) -> media.getOrderIndex() == null ? Integer.MAX_VALUE : media.getOrderIndex())
                    .thenComparing(media -> media.getId() == null ? Long.MAX_VALUE : media.getId());

    private static final Comparator<CollectionProject> COLLECTION_LINK_ORDER =
            Comparator.comparing((CollectionProject link) -> link.getAddedDate(), Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(link -> link.getId() == null ? Long.MAX_VALUE : link.getId());

    public PortfolioResponse toPortfolioResponse(
            Portfolio portfolio,
            List<PortfolioProject> projects,
            List<PortfolioCollection> collections,
            boolean includeOwnerEmail) {
        return PortfolioResponse.builder()
                .portfolio(toPortfolioData(portfolio))
                .owner(toOwner(portfolio.getUser(), includeOwnerEmail))
                .profile(toProfile(portfolio.getUser().getProfile()))
                .projects(projects.stream().map(this::toProjectResponse).toList())
                .collections(collections.stream().map(this::toCollectionResponse).toList())
                .build();
    }

    public PortfolioProjectResponse toProjectResponse(PortfolioProject project) {
        String imageUrl = firstMediaUrl(project, MediaType.IMAGE);
        String videoUrl = firstMediaUrl(project, MediaType.VIDEO);
        String thumbnailUrl = thumbnailUrl(project);
        return PortfolioProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectUrl(project.getProjectUrl())
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .pinned(Boolean.TRUE.equals(project.getPinned()))
                .visibility(project.getVisibility())
                .totalLikes(project.getLikes() == null ? 0 : project.getLikes())
                .skills(project.getSkills().stream().sorted(SKILL_ORDER).map(this::toSkillSummary).toList())
                .media(project.getMedia().stream().sorted(MEDIA_ORDER).map(this::toProjectMediaResponse).toList())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public PortfolioCollectionResponse toCollectionResponse(PortfolioCollection collection) {
        return PortfolioCollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .visibility(collection.getVisibility())
                .totalLikes(collection.getLikes() == null ? 0 : collection.getLikes())
                .projects(collection.getCollectionProjects().stream()
                        .sorted(COLLECTION_LINK_ORDER)
                        .map(CollectionProject::getProject)
                        .filter(Objects::nonNull)
                        .map(this::toProjectSummary)
                        .toList())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    public SkillSummaryDto toSkillSummary(Skill skill) {
        return SkillSummaryDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }

    private PortfolioDataDto toPortfolioData(Portfolio portfolio) {
        return PortfolioDataDto.builder()
                .id(portfolio.getId())
                .title(portfolio.getTitle())
                .bio(portfolio.getBio())
                .coverImage(portfolio.getCoverImage())
                .job(portfolio.getJob())
                .githubUrl(portfolio.getGithubUrl())
                .linkedinUrl(portfolio.getLinkedinUrl())
                .openToWork(portfolio.isOpenToWork())
                .availableForFreelance(portfolio.isAvailableForFreelance())
                .visibility(portfolio.getVisibility())
                .totalViews(portfolio.getTotalViews() == null ? 0L : portfolio.getTotalViews())
                .verified(portfolio.isVerified())
                .skills(portfolio.getSkills().stream().sorted(SKILL_ORDER).map(this::toSkillSummary).toList())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    private PortfolioOwnerDto toOwner(User user, boolean includeOwnerEmail) {
        return PortfolioOwnerDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(includeOwnerEmail ? user.getEmail() : null)
                .build();
    }

    private PortfolioProfileDto toProfile(Profile profile) {
        if (profile == null) {
            return PortfolioProfileDto.builder().build();
        }
        return PortfolioProfileDto.builder()
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .profilePicture(profile.getProfilePicture())
                .location(null)
                .interests(profile.getInterests())
                .build();
    }

    private ProjectSummaryDto toProjectSummary(PortfolioProject project) {
        String imageUrl = firstMediaUrl(project, MediaType.IMAGE);
        String videoUrl = firstMediaUrl(project, MediaType.VIDEO);
        String thumbnailUrl = thumbnailUrl(project);
        return ProjectSummaryDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .coverImage(thumbnailUrl)
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .visibility(project.getVisibility())
                .build();
    }

    private ProjectMediaResponse toProjectMediaResponse(ProjectMedia media) {
        return ProjectMediaResponse.builder()
                .id(media.getId())
                .mediaUrl(media.getMediaUrl())
                .imageUrl(media.getMediaType() == MediaType.IMAGE ? media.getMediaUrl() : null)
                .videoUrl(media.getMediaType() == MediaType.VIDEO ? media.getMediaUrl() : null)
                .thumbnailUrl(media.getMediaType() == MediaType.IMAGE ? media.getMediaUrl() : null)
                .mediaType(media.getMediaType())
                .orderIndex(media.getOrderIndex())
                .build();
    }

    private String firstMediaUrl(PortfolioProject project, MediaType type) {
        if (project.getMedia() == null) {
            return null;
        }
        return project.getMedia().stream()
                .filter(Objects::nonNull)
                .filter(media -> media.getMediaType() == type)
                .sorted(MEDIA_ORDER)
                .map(ProjectMedia::getMediaUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String thumbnailUrl(PortfolioProject project) {
        String imageUrl = firstMediaUrl(project, MediaType.IMAGE);
        if (imageUrl != null) {
            return imageUrl;
        }
        if (project.getMedia() == null) {
            return null;
        }
        return project.getMedia().stream()
                .filter(Objects::nonNull)
                .sorted(MEDIA_ORDER)
                .map(ProjectMedia::getMediaUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
