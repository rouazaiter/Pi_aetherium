package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.TechnicalDepthDto;
import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.ProjectMedia;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicalDepthServiceImplTest {

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private PortfolioRepository portfolioRepository;

    @Test
    void analyzeCurrentUserDetectsBackendDepthAndMissingAreas() {
        TechnicalDepthServiceImpl service = new TechnicalDepthServiceImpl(currentUserService, portfolioRepository);

        User user = User.builder().id(1L).build();
        PortfolioProject project = PortfolioProject.builder()
                .id(10L)
                .title("Education API")
                .description("Built a Java Spring Boot REST API with JPA, Hibernate, PostgreSQL, JWT authentication, and Docker deployment.")
                .projectUrl("https://example.com")
                .pinned(true)
                .media(List.of(ProjectMedia.builder().id(1L).mediaUrl("https://img").build()))
                .skills(Set.of(
                        Skill.builder().name("Java").category(SkillCategory.BACKEND_DEVELOPMENT).build(),
                        Skill.builder().name("Spring Boot").category(SkillCategory.BACKEND_DEVELOPMENT).build(),
                        Skill.builder().name("PostgreSQL").category(SkillCategory.SQL_DATABASES).build(),
                        Skill.builder().name("JWT").category(SkillCategory.APPLICATION_SECURITY).build(),
                        Skill.builder().name("Docker").category(SkillCategory.CONTAINERIZATION).build()
                ))
                .build();
        Portfolio portfolio = Portfolio.builder()
                .user(User.builder().username("jihen").build())
                .skills(Set.of(
                        Skill.builder().name("Java").category(SkillCategory.BACKEND_DEVELOPMENT).build(),
                        Skill.builder().name("Spring Boot").category(SkillCategory.BACKEND_DEVELOPMENT).build(),
                        Skill.builder().name("PostgreSQL").category(SkillCategory.SQL_DATABASES).build(),
                        Skill.builder().name("JWT").category(SkillCategory.APPLICATION_SECURITY).build(),
                        Skill.builder().name("Docker").category(SkillCategory.CONTAINERIZATION).build()
                ))
                .projects(Set.of(project))
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(portfolioRepository.findByUser_Id(1L)).thenReturn(Optional.of(portfolio));

        TechnicalDepthDto result = service.analyzeCurrentUser();

        assertEquals("BACKEND", result.getDominantFamily().name());
        assertTrue(result.getDepthScore() > 0);
        assertTrue(result.getStrongSignals().stream().anyMatch(signal -> signal.toLowerCase().contains("backend")));
        assertTrue(result.getMissingDepthAreas().stream().anyMatch(area -> area.equals("CI/CD") || area.equals("Monitoring") || area.equals("Testing")));
    }
}
