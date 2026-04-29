package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.SkillCoverageDto;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.ai.SkillCoverageService;
import com.education.platform.services.interfaces.portfolio.ai.TechnicalDepthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SkillCoverageServiceImpl extends AbstractPortfolioAiService implements SkillCoverageService {

    private final TechnicalDepthService technicalDepthService;

    public SkillCoverageServiceImpl(
            CurrentUserService currentUserService,
            PortfolioRepository portfolioRepository,
            TechnicalDepthService technicalDepthService) {
        super(currentUserService, portfolioRepository);
        this.technicalDepthService = technicalDepthService;
    }

    @Override
    @Transactional(readOnly = true)
    public SkillCoverageDto analyzeCurrentUser() {
        Portfolio portfolio = getCurrentUserPortfolio();
        DeveloperFamily family = technicalDepthService.analyzeCurrentUser().getDominantFamily();
        Set<SkillCategory> categories = PortfolioAiSupport.skillCategories(portfolio.getSkills());

        CoverageResult result = switch (family) {
            case BACKEND -> backendCoverage(categories);
            case FRONTEND -> frontendCoverage(categories);
            case FULL_STACK -> fullStackCoverage(categories);
            case DEVOPS_CLOUD -> devopsCoverage(categories);
            case DATA_AI -> dataAiCoverage(categories);
            case SECURITY -> securityCoverage(categories);
            case DESIGN_CREATIVE -> designCoverage(categories);
            case GENERAL -> new CoverageResult(0, List.of(), List.of());
        };

        List<String> recommendations = family == DeveloperFamily.GENERAL
                ? List.of(
                "Add more clearly categorized skills to define your technical direction.",
                "Strengthen your portfolio title and bio so your profile communicates one clear identity.",
                "Add projects with skills attached to make your specialization easier to detect."
        )
                : result.missingAreas().stream().map(area -> "Add skills or project proof related to " + area + ".").toList();

        return SkillCoverageDto.builder()
                .dominantFamily(family)
                .coverageScore(result.score())
                .coveredAreas(result.coveredAreas())
                .missingAreas(result.missingAreas())
                .recommendations(recommendations)
                .build();
    }

    private CoverageResult backendCoverage(Set<SkillCategory> categories) {
        return fivePillarCoverage(categories,
                new Pillar("Core Backend", Set.of(SkillCategory.BACKEND_DEVELOPMENT)),
                new Pillar("Databases", Set.of(SkillCategory.SQL_DATABASES, SkillCategory.NOSQL_DATABASES, SkillCategory.DATABASE_ADMINISTRATION)),
                new Pillar("Testing & Quality", Set.of(SkillCategory.QUALITY_ASSURANCE)),
                new Pillar("Deployment & DevOps", Set.of(SkillCategory.DEVOPS, SkillCategory.CONTAINERIZATION, SkillCategory.CI_CD, SkillCategory.CLOUD_COMPUTING, SkillCategory.INFRASTRUCTURE_AS_CODE, SkillCategory.MONITORING)),
                new Pillar("Security", Set.of(SkillCategory.APPLICATION_SECURITY, SkillCategory.IDENTITY_ACCESS_MANAGEMENT, SkillCategory.NETWORK_SECURITY, SkillCategory.SECURITY_OPERATIONS, SkillCategory.DEVSECOPS))
        );
    }

    private CoverageResult frontendCoverage(Set<SkillCategory> categories) {
        return fivePillarCoverage(categories,
                new Pillar("Core Frontend", Set.of(SkillCategory.FRONTEND_DEVELOPMENT, SkillCategory.MOBILE_DEVELOPMENT)),
                new Pillar("UI / UX", Set.of(SkillCategory.UI_UX_DESIGN)),
                new Pillar("Testing & Quality", Set.of(SkillCategory.QUALITY_ASSURANCE)),
                new Pillar("Content & Communication", Set.of(SkillCategory.CONTENT_STRATEGY, SkillCategory.TECHNICAL_WRITING, SkillCategory.COPYWRITING)),
                new Pillar("Full Experience & Product", Set.of(SkillCategory.FULL_STACK_DEVELOPMENT, SkillCategory.E_COMMERCE, SkillCategory.DIGITAL_MARKETING, SkillCategory.SEO_STRATEGY, SkillCategory.PRODUCT_MANAGEMENT))
        );
    }

    private CoverageResult fullStackCoverage(Set<SkillCategory> categories) {
        return fivePillarCoverage(categories,
                new Pillar("Full Stack Core", Set.of(SkillCategory.FULL_STACK_DEVELOPMENT)),
                new Pillar("Frontend Layer", Set.of(SkillCategory.FRONTEND_DEVELOPMENT, SkillCategory.UI_UX_DESIGN, SkillCategory.MOBILE_DEVELOPMENT)),
                new Pillar("Backend Layer", Set.of(SkillCategory.BACKEND_DEVELOPMENT, SkillCategory.APPLICATION_SECURITY)),
                new Pillar("Data Layer", Set.of(SkillCategory.SQL_DATABASES, SkillCategory.NOSQL_DATABASES, SkillCategory.DATABASE_ADMINISTRATION)),
                new Pillar("Delivery & Reliability", Set.of(SkillCategory.CONTAINERIZATION, SkillCategory.CI_CD, SkillCategory.CLOUD_COMPUTING, SkillCategory.QUALITY_ASSURANCE, SkillCategory.MONITORING))
        );
    }

    private CoverageResult devopsCoverage(Set<SkillCategory> categories) {
        return fivePillarCoverage(categories,
                new Pillar("Core DevOps / Cloud", Set.of(SkillCategory.DEVOPS, SkillCategory.CLOUD_COMPUTING)),
                new Pillar("Infrastructure & Containers", Set.of(SkillCategory.CONTAINERIZATION, SkillCategory.INFRASTRUCTURE_AS_CODE)),
                new Pillar("CI / CD Automation", Set.of(SkillCategory.CI_CD)),
                new Pillar("Observability & Monitoring", Set.of(SkillCategory.MONITORING)),
                new Pillar("Security & Hardening", Set.of(SkillCategory.DEVSECOPS, SkillCategory.SECURITY_OPERATIONS, SkillCategory.NETWORK_SECURITY, SkillCategory.APPLICATION_SECURITY))
        );
    }

    private CoverageResult dataAiCoverage(Set<SkillCategory> categories) {
        return fivePillarCoverage(categories,
                new Pillar("Data Foundations", Set.of(SkillCategory.DATA_ANALYSIS, SkillCategory.DATA_ENGINEERING, SkillCategory.BUSINESS_ANALYSIS, SkillCategory.BUSINESS_INTELLIGENCE)),
                new Pillar("Modeling & Learning", Set.of(SkillCategory.MACHINE_LEARNING, SkillCategory.DEEP_LEARNING)),
                new Pillar("Applied AI Specialization", Set.of(SkillCategory.NATURAL_LANGUAGE_PROCESSING, SkillCategory.COMPUTER_VISION, SkillCategory.LLM_ENGINEERING)),
                new Pillar("Data Storage & Access", Set.of(SkillCategory.SQL_DATABASES, SkillCategory.NOSQL_DATABASES)),
                new Pillar("Deployment & Production Readiness", Set.of(SkillCategory.CLOUD_COMPUTING, SkillCategory.CONTAINERIZATION, SkillCategory.CI_CD))
        );
    }

    private CoverageResult securityCoverage(Set<SkillCategory> categories) {
        return fivePillarCoverage(categories,
                new Pillar("Core Security", Set.of(SkillCategory.NETWORK_SECURITY, SkillCategory.APPLICATION_SECURITY)),
                new Pillar("Identity & Access", Set.of(SkillCategory.IDENTITY_ACCESS_MANAGEMENT)),
                new Pillar("Security Operations", Set.of(SkillCategory.SECURITY_OPERATIONS, SkillCategory.MONITORING)),
                new Pillar("Secure Delivery", Set.of(SkillCategory.DEVSECOPS, SkillCategory.CI_CD, SkillCategory.CONTAINERIZATION)),
                new Pillar("Infrastructure Security", Set.of(SkillCategory.CLOUD_COMPUTING, SkillCategory.INFRASTRUCTURE_AS_CODE))
        );
    }

    private CoverageResult designCoverage(Set<SkillCategory> categories) {
        return fivePillarCoverage(categories,
                new Pillar("Core Design", Set.of(SkillCategory.UI_UX_DESIGN, SkillCategory.GRAPHIC_DESIGN, SkillCategory.ILLUSTRATION)),
                new Pillar("Visual Production", Set.of(SkillCategory.MOTION_GRAPHICS, SkillCategory.VIDEO_EDITING, SkillCategory.THREE_D_MODELING)),
                new Pillar("Media Content", Set.of(SkillCategory.PHOTOGRAPHY, SkillCategory.AUDIO_PRODUCTION, SkillCategory.MUSIC_COMPOSITION, SkillCategory.VOICE_OVER, SkillCategory.PODCAST_PRODUCTION)),
                new Pillar("Narrative & Communication", Set.of(SkillCategory.COPYWRITING, SkillCategory.CONTENT_WRITING, SkillCategory.TECHNICAL_WRITING, SkillCategory.SCRIPT_WRITING)),
                new Pillar("Audience & Product Reach", Set.of(SkillCategory.DIGITAL_MARKETING, SkillCategory.CONTENT_STRATEGY, SkillCategory.SEO_STRATEGY, SkillCategory.SOCIAL_MEDIA_MARKETING, SkillCategory.E_COMMERCE))
        );
    }

    private CoverageResult fivePillarCoverage(Set<SkillCategory> categories, Pillar... pillars) {
        List<String> covered = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int score = 0;
        for (Pillar pillar : pillars) {
            boolean matched = pillar.categories().stream().anyMatch(categories::contains);
            if (matched) {
                covered.add(pillar.label());
                score += 20;
            } else {
                missing.add(pillar.label());
            }
        }
        return new CoverageResult(score, covered, missing);
    }

    private record CoverageResult(int score, List<String> coveredAreas, List<String> missingAreas) {
    }

    private record Pillar(String label, Set<SkillCategory> categories) {
    }
}
