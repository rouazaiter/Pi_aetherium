package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.ProfileCoherenceDto;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioCoherenceService;
import com.education.platform.services.interfaces.portfolio.ai.TechnicalDepthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PortfolioCoherenceServiceImpl extends AbstractPortfolioAiService implements PortfolioCoherenceService {

    private final TechnicalDepthService technicalDepthService;

    public PortfolioCoherenceServiceImpl(
            CurrentUserService currentUserService,
            PortfolioRepository portfolioRepository,
            TechnicalDepthService technicalDepthService) {
        super(currentUserService, portfolioRepository);
        this.technicalDepthService = technicalDepthService;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileCoherenceDto analyzeCurrentUser() {
        Portfolio portfolio = getCurrentUserPortfolio();
        DeveloperFamily family = technicalDepthService.analyzeCurrentUser().getDominantFamily();

        int skillAlignmentScore = calculateSkillAlignmentScore(portfolio, family);
        int titleAlignmentScore = calculateTitleAlignmentScore(portfolio, family);
        int bioAlignmentScore = calculateBioAlignmentScore(portfolio, family);
        int projectProofScore = calculateProjectProofScore(portfolio, family);
        int totalScore = skillAlignmentScore + titleAlignmentScore + bioAlignmentScore + projectProofScore;
        boolean mismatchDetected = family != DeveloperFamily.GENERAL && titleAlignmentScore == 0 && bioAlignmentScore == 0;

        return ProfileCoherenceDto.builder()
                .titleAlignmentScore(titleAlignmentScore)
                .bioAlignmentScore(bioAlignmentScore)
                .skillAlignmentScore(skillAlignmentScore)
                .projectProofScore(projectProofScore)
                .totalScore(totalScore)
                .status(coherenceStatus(totalScore, mismatchDetected))
                .mismatchDetected(mismatchDetected)
                .recommendations(buildRecommendations(titleAlignmentScore, bioAlignmentScore, skillAlignmentScore, projectProofScore, mismatchDetected, family))
                .build();
    }

    private int calculateSkillAlignmentScore(Portfolio portfolio, DeveloperFamily family) {
        if (family == DeveloperFamily.GENERAL || portfolio.getSkills() == null || portfolio.getSkills().isEmpty()) {
            return 0;
        }
        double total = 0;
        double matched = 0;
        Set<com.education.platform.entities.portfolio.SkillCategory> categories = PortfolioAiSupport.skillCategories(portfolio.getSkills());
        for (var category : categories) {
            total += 1.0;
            matched += SkillFamilyWeightMapper.mapWeights(category).getOrDefault(family, 0.0) >= 0.5 ? 1.0 : 0.0;
        }
        double ratio = total == 0 ? 0 : matched / total;
        if (ratio >= 0.80) return 40;
        if (ratio >= 0.65) return 32;
        if (ratio >= 0.50) return 24;
        if (ratio >= 0.35) return 16;
        return 8;
    }

    private int calculateTitleAlignmentScore(Portfolio portfolio, DeveloperFamily family) {
        String text = ((portfolio.getJob() == null ? "" : portfolio.getJob()) + " " + (portfolio.getTitle() == null ? "" : portfolio.getTitle())).toLowerCase();
        if (text.isBlank() || family == DeveloperFamily.GENERAL) return 0;

        switch (family) {
            case BACKEND -> {
                if (text.contains("backend developer") || text.contains("backend engineer")) return 20;
            }
            case FRONTEND -> {
                if (text.contains("frontend developer") || text.contains("frontend engineer")) return 20;
            }
            case FULL_STACK -> {
                if (text.contains("full stack developer") || text.contains("fullstack developer")) return 20;
            }
            case DEVOPS_CLOUD -> {
                if (text.contains("devops engineer") || text.contains("cloud engineer")) return 20;
            }
            case DATA_AI -> {
                if (text.contains("data engineer") || text.contains("data analyst") || text.contains("machine learning engineer") || text.contains("ai engineer")) return 20;
            }
            case SECURITY -> {
                if (text.contains("security engineer") || text.contains("cybersecurity engineer")) return 20;
            }
            case DESIGN_CREATIVE -> {
                if (text.contains("ui/ux designer") || text.contains("graphic designer") || text.contains("product designer")) return 20;
            }
            default -> {
            }
        }

        long matches = PortfolioAiSupport.familyKeywords(family).stream().filter(text::contains).count();
        if (matches >= 2) return 16;
        if (matches == 1) return 10;
        return 0;
    }

    private int calculateBioAlignmentScore(Portfolio portfolio, DeveloperFamily family) {
        if (!PortfolioAiSupport.hasText(portfolio.getBio()) || family == DeveloperFamily.GENERAL) return 0;
        String lower = portfolio.getBio().toLowerCase();
        long matches = PortfolioAiSupport.familyKeywords(family).stream().filter(lower::contains).count();
        if (matches >= 4) return 20;
        if (matches >= 3) return 16;
        if (matches == 2) return 12;
        if (matches == 1) return 6;
        return 0;
    }

    private int calculateProjectProofScore(Portfolio portfolio, DeveloperFamily family) {
        List<PortfolioProject> projects = PortfolioAiSupport.sortedProjects(portfolio);
        if (projects.isEmpty() || family == DeveloperFamily.GENERAL) return 0;
        long matchingProjects = projects.stream().filter(project -> PortfolioAiSupport.projectMatchesFamily(project, family)).count();
        double ratio = (double) matchingProjects / projects.size();
        if (ratio >= 0.80) return 20;
        if (ratio >= 0.60) return 16;
        if (ratio >= 0.40) return 12;
        if (ratio >= 0.20) return 8;
        return 4;
    }

    private String coherenceStatus(int totalScore, boolean mismatchDetected) {
        if (mismatchDetected) return "MISMATCH";
        if (totalScore >= 75) return "HIGH";
        if (totalScore >= 45) return "MEDIUM";
        return "LOW";
    }

    private List<String> buildRecommendations(int titleScore, int bioScore, int skillScore, int projectScore, boolean mismatchDetected, DeveloperFamily family) {
        List<String> recommendations = new ArrayList<>();
        if (mismatchDetected) {
            recommendations.add("Your written positioning does not match your strongest technical signals. Choose one clearer direction.");
        }
        if (titleScore < 10) recommendations.add("Improve the title or job line so it reflects your real " + PortfolioAiSupport.formatFamilyLabel(family).toLowerCase() + " direction.");
        if (bioScore < 10) recommendations.add("Rewrite the bio so it reinforces the same direction as your skills.");
        if (projectScore < 12) recommendations.add("Make projects support the same story as the title, bio, and skill set.");
        if (skillScore < 24) recommendations.add("Reduce skill scatter and strengthen the categories that support your main direction.");
        return recommendations.stream().distinct().toList();
    }
}
