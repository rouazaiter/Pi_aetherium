package com.education.platform.services.implementations.cv;

import com.education.platform.dto.cv.CVPreviewResponse;
import com.education.platform.dto.cv.CVPreviewOptions;
import com.education.platform.entities.cv.CVEducationEntry;
import com.education.platform.entities.cv.CVExperienceEntry;
import com.education.platform.entities.cv.CVLanguageEntry;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVProfile;
import com.education.platform.entities.portfolio.CollectionProject;
import com.education.platform.entities.portfolio.MediaType;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioCollection;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.ProjectMedia;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.cv.CVProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CVBuilderServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioProjectRepository portfolioProjectRepository;

    @Mock
    private CVProfileService cvProfileService;

    @InjectMocks
    private CVBuilderServiceImpl cvBuilderService;

    @Test
    void buildForUserBuildsPreviewFromPortfolioAndProjects() {
        User user = User.builder()
                .id(7L)
                .username("jdoe")
                .email("jane@example.com")
                .build();
        user.setProfile(Profile.builder()
                .firstName("Jane")
                .lastName("Doe")
                .description("Profile description")
                .build());

        Skill java = Skill.builder()
                .id(1L)
                .name("Java")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();
        Skill spring = Skill.builder()
                .id(2L)
                .name("Spring Boot")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();
        Skill angular = Skill.builder()
                .id(3L)
                .name("Angular")
                .category(SkillCategory.FRONTEND_DEVELOPMENT)
                .build();

        Portfolio portfolio = Portfolio.builder()
                .id(11L)
                .title("Platform Engineer")
                .bio("Portfolio bio")
                .job("Senior Backend Engineer")
                .githubUrl("https://github.com/jane")
                .linkedinUrl("https://linkedin.com/in/jane")
                .skills(new LinkedHashSet<>(List.of(java)))
                .build();

        PortfolioCollection featuredCollection = PortfolioCollection.builder()
                .name("Featured Work")
                .build();

        PortfolioProject olderProject = PortfolioProject.builder()
                .id(101L)
                .title("Older Project")
                .description("Old")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 10, 0))
                .visibility(Visibility.PUBLIC)
                .skills(new LinkedHashSet<>(List.of(spring)))
                .build();

        PortfolioProject newerProject = PortfolioProject.builder()
                .id(102L)
                .title("Newer Project")
                .description("New")
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 2, 10, 0))
                .visibility(Visibility.FRIENDS_ONLY)
                .skills(new LinkedHashSet<>(List.of(java, angular)))
                .build();
        newerProject.setMedia(List.of(ProjectMedia.builder()
                .mediaType(MediaType.IMAGE)
                .mediaUrl("https://cdn.example.com/project.png")
                .orderIndex(1)
                .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .project(newerProject)
                .build()));
        newerProject.setCollectionProjects(Set.of(CollectionProject.builder()
                .project(newerProject)
                .portfolioCollection(featuredCollection)
                .addedDate(LocalDateTime.of(2025, 1, 3, 10, 0))
                .orderIndex(1)
                .build()));

        CVProfile cvProfile = CVProfile.builder()
                .headline("Custom headline")
                .summary("Custom summary")
                .phone("+123456789")
                .location("Lagos")
                .preferredTemplate("modern")
                .language("fr")
                .visibility(Visibility.PUBLIC)
                .selectedProjectIds(List.of(102L, 101L))
                .education(List.of(CVEducationEntry.builder().school("University A").degree("BSc").build()))
                .experience(List.of(CVExperienceEntry.builder().company("Acme").role("Engineer").summary("Built APIs").build()))
                .languages(List.of(CVLanguageEntry.builder().name("English").proficiency("Fluent").build()))
                .build();

        when(cvProfileService.findEntityForUser(user)).thenReturn(Optional.of(cvProfile));
        when(portfolioRepository.findByUser_Id(7L)).thenReturn(Optional.of(portfolio));
        when(portfolioProjectRepository.findByPortfolio_IdOrderByCreatedAtDesc(11L))
                .thenReturn(List.of(olderProject, newerProject));

        CVPreviewResponse result = cvBuilderService.buildForUser(user);

        assertNotNull(result.getProfile());
        assertEquals("Jane Doe", result.getProfile().getFullName());
        assertEquals("jane@example.com", result.getProfile().getEmail());
        assertEquals("+123456789", result.getProfile().getPhone());
        assertEquals("Lagos", result.getProfile().getLocation());
        assertEquals("Custom headline", result.getProfile().getHeadline());
        assertEquals("Custom summary", result.getProfile().getSummary());
        assertEquals("https://github.com/jane", result.getProfile().getGithubUrl());
        assertEquals("https://linkedin.com/in/jane", result.getProfile().getLinkedInUrl());
        assertEquals("modern", result.getProfile().getPreferredTemplate());
        assertEquals("fr", result.getProfile().getLanguage());
        assertEquals(Visibility.PUBLIC, result.getProfile().getVisibility());

        assertNotNull(result.getSkillsByCategory());
        assertEquals(2, result.getSkillsByCategory().size());
        assertEquals(List.of("Java", "Spring Boot"), result.getSkillsByCategory()
                .stream().filter(group -> "BACKEND_DEVELOPMENT".equals(group.getCategory())).findFirst().orElseThrow()
                .getSkills()
                .stream()
                .map(skill -> skill.getName())
                .toList());
        assertEquals(List.of("Angular"), result.getSkillsByCategory()
                .stream().filter(group -> "FRONTEND_DEVELOPMENT".equals(group.getCategory())).findFirst().orElseThrow()
                .getSkills()
                .stream()
                .map(skill -> skill.getName())
                .toList());

        assertNotNull(result.getProjects());
        assertEquals(List.of("Newer Project", "Older Project"), result.getProjects().stream().map(project -> project.getTitle()).toList());
        assertTrue(result.getProjects().stream().allMatch(project -> project.getSkills() != null && !project.getSkills().isEmpty()));
        assertEquals("https://cdn.example.com/project.png", result.getProjects().get(0).getImageUrl());
        assertEquals("Featured Work", result.getProjects().get(0).getCollectionName());
        assertEquals(Visibility.FRIENDS_ONLY, result.getProjects().get(0).getVisibility());
        assertNotNull(result.getEducation());
        assertNotNull(result.getExperience());
        assertNotNull(result.getLanguages());
        assertNotNull(result.getMeta());
    }

    @Test
    void buildForUserRemovesDuplicateSkillsAndAppliesProjectLimit() {
        User user = User.builder()
                .id(9L)
                .username("jdoe")
                .build();

        Skill java = Skill.builder()
                .id(1L)
                .name("Java")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();
        Skill uncategorized = Skill.builder()
                .id(2L)
                .name("Team Leadership")
                .category(null)
                .build();

        Portfolio portfolio = Portfolio.builder()
                .id(20L)
                .skills(new LinkedHashSet<>(List.of(java, uncategorized)))
                .build();

        PortfolioProject emptyProject = PortfolioProject.builder()
                .id(200L)
                .createdAt(LocalDateTime.of(2026, 1, 3, 10, 0))
                .skills(new LinkedHashSet<>())
                .build();

        PortfolioProject newestProject = PortfolioProject.builder()
                .id(201L)
                .title("Newest")
                .createdAt(LocalDateTime.of(2026, 1, 2, 10, 0))
                .skills(new LinkedHashSet<>(List.of(java)))
                .build();

        PortfolioProject duplicateNewestProject = PortfolioProject.builder()
                .id(201L)
                .title("Newest Duplicate")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .skills(new LinkedHashSet<>(List.of(java)))
                .build();

        PortfolioProject secondProject = PortfolioProject.builder()
                .id(202L)
                .title("Second")
                .createdAt(LocalDateTime.of(2025, 12, 30, 10, 0))
                .skills(new LinkedHashSet<>(List.of(uncategorized)))
                .build();

        when(cvProfileService.findEntityForUser(user)).thenReturn(Optional.empty());
        when(portfolioRepository.findByUser_Id(9L)).thenReturn(Optional.of(portfolio));
        when(portfolioProjectRepository.findByPortfolio_IdOrderByCreatedAtDesc(20L))
                .thenReturn(List.of(emptyProject, newestProject, duplicateNewestProject, secondProject));

        CVPreviewResponse result = cvBuilderService.buildForUser(user, CVPreviewOptions.builder()
                .projectLimit(1)
                .template("unsupported-template")
                .build());

        assertNotNull(result.getProfile());
        assertEquals("jdoe", result.getProfile().getFullName());
        assertEquals("developer-minimal", result.getProfile().getPreferredTemplate());
        assertEquals("en", result.getProfile().getLanguage());
        assertEquals(Visibility.PRIVATE, result.getProfile().getVisibility());
        assertNotNull(result.getMeta());

        assertNotNull(result.getSkillsByCategory());
        assertEquals(2, result.getSkillsByCategory().size());
        assertEquals(List.of("Java"), result.getSkillsByCategory().stream()
                .filter(group -> "BACKEND_DEVELOPMENT".equals(group.getCategory()))
                .findFirst().orElseThrow()
                .getSkills().stream().map(skill -> skill.getName()).toList());
        assertEquals(List.of("Team Leadership"), result.getSkillsByCategory().stream()
                .filter(group -> "OTHER".equals(group.getCategory()))
                .findFirst().orElseThrow()
                .getSkills().stream().map(skill -> skill.getName()).toList());

        assertNotNull(result.getProjects());
        assertEquals(1, result.getProjects().size());
        assertEquals("Newest", result.getProjects().get(0).getTitle());
    }

    @Test
    void buildForUserUsesDefaultsWhenCvProfileIsMissing() {
        User user = User.builder()
                .id(12L)
                .username("fallback-user")
                .build();

        when(cvProfileService.findEntityForUser(user)).thenReturn(Optional.empty());
        when(portfolioRepository.findByUser_Id(12L)).thenReturn(Optional.empty());

        CVPreviewResponse result = cvBuilderService.buildForUser(user);

        assertNotNull(result.getProfile());
        assertEquals("fallback-user", result.getProfile().getFullName());
        assertEquals("developer-minimal", result.getProfile().getPreferredTemplate());
        assertEquals("en", result.getProfile().getLanguage());
        assertEquals(Visibility.PRIVATE, result.getProfile().getVisibility());
        assertNull(result.getProjects());
        assertNull(result.getSkillsByCategory());
        assertNotNull(result.getMeta());
    }
}
