package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.ProjectProofDetailDto;
import com.education.platform.dto.portfolio.ai.ProjectProofStrengthDto;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.ai.ProjectProofStrengthService;
import com.education.platform.services.interfaces.portfolio.ai.TechnicalDepthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectProofStrengthServiceImpl extends AbstractPortfolioAiService implements ProjectProofStrengthService {

    private final TechnicalDepthService technicalDepthService;

    public ProjectProofStrengthServiceImpl(
            CurrentUserService currentUserService,
            PortfolioRepository portfolioRepository,
            TechnicalDepthService technicalDepthService) {
        super(currentUserService, portfolioRepository);
        this.technicalDepthService = technicalDepthService;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectProofStrengthDto analyzeCurrentUser() {
        Portfolio portfolio = getCurrentUserPortfolio();
        List<PortfolioProject> projects = PortfolioAiSupport.sortedProjects(portfolio);
        DeveloperFamily family = technicalDepthService.analyzeCurrentUser().getDominantFamily();

        if (family == DeveloperFamily.GENERAL || projects.isEmpty()) {
            return ProjectProofStrengthDto.builder()
                    .dominantFamily(family)
                    .proofScore(0)
                    .strongProjects(0)
                    .weakProjects(projects.size())
                    .projectDetails(List.of())
                    .recommendations(List.of(
                            "Add projects with clearly defined technical skills.",
                            "Write stronger project descriptions that explain the work done.",
                            "Include demos, repositories, screenshots, or deployment links."
                    ))
                    .build();
        }

        List<ProjectProofDetailDto> details = new ArrayList<>();
        int totalRawScore = 0;
        int strongProjects = 0;
        int weakProjects = 0;

        for (PortfolioProject project : projects) {
            ProjectProofDetailDto detail = analyzeProject(project, family);
            details.add(detail);
            totalRawScore += detail.getScore();
            if ("STRONG".equals(detail.getLevel())) strongProjects++;
            if ("WEAK".equals(detail.getLevel())) weakProjects++;
        }

        int proofScore = details.isEmpty() ? 0 : Math.min(100, (int) Math.round((totalRawScore / (double) details.size()) * 5));

        return ProjectProofStrengthDto.builder()
                .dominantFamily(family)
                .proofScore(proofScore)
                .strongProjects(strongProjects)
                .weakProjects(weakProjects)
                .projectDetails(details)
                .recommendations(buildRecommendations(family, strongProjects, weakProjects, details))
                .build();
    }

    private ProjectProofDetailDto analyzeProject(PortfolioProject project, DeveloperFamily family) {
        boolean hasDescription = PortfolioAiSupport.hasText(project.getDescription());
        boolean detailedDescription = hasDescription && project.getDescription().trim().length() >= 80;
        boolean hasUrl = PortfolioAiSupport.hasText(project.getProjectUrl());
        boolean hasMedia = project.getMedia() != null && !project.getMedia().isEmpty();
        boolean hasLinkedSkills = project.getSkills() != null && !project.getSkills().isEmpty();
        boolean pinned = Boolean.TRUE.equals(project.getPinned());
        boolean familyMatch = PortfolioAiSupport.projectMatchesFamily(project, family);

        int score = 0;
        if (familyMatch) score += 6;
        if (hasLinkedSkills) score += 4;
        if (hasDescription) score += 3;
        if (detailedDescription) score += 2;
        if (hasUrl) score += 2;
        if (hasMedia) score += 2;
        if (pinned) score += 1;

        List<String> strengths = new ArrayList<>();
        if (familyMatch) strengths.add("Supports the main " + PortfolioAiSupport.formatFamilyLabel(family).toLowerCase() + " direction.");
        if (hasLinkedSkills) strengths.add("Has linked skills.");
        if (detailedDescription) strengths.add("Description is detailed enough to explain the work.");
        if (hasUrl) strengths.add("Has a visible project link.");
        if (hasMedia) strengths.add("Has media proof.");
        if (pinned) strengths.add("Is pinned for visibility.");

        List<String> issues = new ArrayList<>();
        if (!familyMatch) issues.add("Does not clearly support the main direction.");
        if (!hasDescription) issues.add("Missing description.");
        else if (!detailedDescription) issues.add("Description is too short.");
        if (!hasUrl) issues.add("Missing project URL.");
        if (!hasMedia) issues.add("Missing screenshots or media.");
        if (!hasLinkedSkills) issues.add("Missing linked skills.");

        return ProjectProofDetailDto.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .score(score)
                .level(projectLevel(score))
                .hasDescription(hasDescription)
                .hasUrl(hasUrl)
                .hasMedia(hasMedia)
                .hasLinkedSkills(hasLinkedSkills)
                .pinned(pinned)
                .strengths(strengths)
                .issues(issues)
                .build();
    }

    private List<String> buildRecommendations(DeveloperFamily family, int strongProjects, int weakProjects, List<ProjectProofDetailDto> details) {
        List<String> recommendations = new ArrayList<>();
        if (strongProjects == 0) recommendations.add("Add at least one project that clearly demonstrates your core specialization.");
        if (weakProjects > 0) recommendations.add("Strengthen weak project descriptions, skills, and proof so they support your technical identity.");
        if (details.stream().anyMatch(detail -> !Boolean.TRUE.equals(detail.getHasUrl()))) recommendations.add("Add project URLs, demos, or repositories to improve credibility.");
        if (details.stream().anyMatch(detail -> !Boolean.TRUE.equals(detail.getHasMedia()))) recommendations.add("Add screenshots or media for projects that already support your main direction.");
        if (family == DeveloperFamily.BACKEND) recommendations.add("Add proof of APIs, databases, authentication, testing, or deployment in project descriptions.");
        if (family == DeveloperFamily.FRONTEND) recommendations.add("Show UI depth, interaction design, responsiveness, and polish more explicitly.");
        if (family == DeveloperFamily.FULL_STACK) recommendations.add("Show end-to-end scope by connecting frontend, backend, and data decisions in the same project.");
        if (family == DeveloperFamily.DEVOPS_CLOUD) recommendations.add("Highlight Docker, CI/CD, hosting, monitoring, and infrastructure outcomes in projects.");
        if (family == DeveloperFamily.DATA_AI) recommendations.add("Show applied data or AI workflows, not only listed tools.");
        if (family == DeveloperFamily.SECURITY) recommendations.add("Highlight authentication, access control, hardening, and security-focused implementation details.");
        if (family == DeveloperFamily.DESIGN_CREATIVE) recommendations.add("Show visual proof, communication quality, and audience-oriented outcomes in project presentation.");
        return recommendations.stream().distinct().toList();
    }

    private String projectLevel(int score) {
        if (score >= 16) return "STRONG";
        if (score >= 9) return "MEDIUM";
        return "WEAK";
    }
}
