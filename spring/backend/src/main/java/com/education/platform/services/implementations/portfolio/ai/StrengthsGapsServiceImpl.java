package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.ProfileCoherenceDto;
import com.education.platform.dto.portfolio.ai.ProjectProofStrengthDto;
import com.education.platform.dto.portfolio.ai.SkillCoverageDto;
import com.education.platform.dto.portfolio.ai.StrengthGapItemDto;
import com.education.platform.dto.portfolio.ai.StrengthsGapsDto;
import com.education.platform.dto.portfolio.ai.TechnicalDepthDto;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioCoherenceService;
import com.education.platform.services.interfaces.portfolio.ai.ProjectProofStrengthService;
import com.education.platform.services.interfaces.portfolio.ai.SkillCoverageService;
import com.education.platform.services.interfaces.portfolio.ai.StrengthsGapsService;
import com.education.platform.services.interfaces.portfolio.ai.TechnicalDepthService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StrengthsGapsServiceImpl implements StrengthsGapsService {

    private final TechnicalDepthService technicalDepthService;
    private final SkillCoverageService skillCoverageService;
    private final ProjectProofStrengthService projectProofStrengthService;
    private final PortfolioCoherenceService portfolioCoherenceService;

    public StrengthsGapsServiceImpl(
            TechnicalDepthService technicalDepthService,
            SkillCoverageService skillCoverageService,
            ProjectProofStrengthService projectProofStrengthService,
            PortfolioCoherenceService portfolioCoherenceService) {
        this.technicalDepthService = technicalDepthService;
        this.skillCoverageService = skillCoverageService;
        this.projectProofStrengthService = projectProofStrengthService;
        this.portfolioCoherenceService = portfolioCoherenceService;
    }

    @Override
    public StrengthsGapsDto analyzeCurrentUser() {
        TechnicalDepthDto depth = technicalDepthService.analyzeCurrentUser();
        SkillCoverageDto coverage = skillCoverageService.analyzeCurrentUser();
        ProjectProofStrengthDto proof = projectProofStrengthService.analyzeCurrentUser();
        ProfileCoherenceDto coherence = portfolioCoherenceService.analyzeCurrentUser();
        DeveloperFamily family = depth.getDominantFamily() == null ? DeveloperFamily.GENERAL : depth.getDominantFamily();

        List<StrengthGapItemDto> strengths = new ArrayList<>();
        List<StrengthGapItemDto> gaps = new ArrayList<>();

        if (depth.getFoundationalScore() != null && depth.getFoundationalScore() >= 24) {
            strengths.add(item("Core " + PortfolioAiSupport.formatFamilyLabel(family), "STRENGTH", 92, "Foundation", "HIGH"));
        }
        if (coverage.getCoveredAreas() != null) {
            coverage.getCoveredAreas().stream().limit(3).forEach(area -> strengths.add(item(area, "STRENGTH", 80, "Coverage", "MEDIUM")));
        }
        if (proof.getStrongProjects() != null && proof.getStrongProjects() > 0) {
            strengths.add(item("Project Proof", "STRENGTH", Math.min(95, 70 + (proof.getStrongProjects() * 8)), "Proof", "HIGH"));
        }
        if (coherence.getTotalScore() != null && coherence.getTotalScore() >= 70) {
            strengths.add(item("Clear " + PortfolioAiSupport.formatFamilyLabel(family) + " Positioning", "STRENGTH", 82, "Coherence", "MEDIUM"));
        }

        if (depth.getMissingDepthAreas() != null) {
            depth.getMissingDepthAreas().stream().limit(4).forEach(area -> gaps.add(item(area, "GAP", 82, "Depth", "HIGH")));
        }
        if (coverage.getMissingAreas() != null) {
            coverage.getMissingAreas().stream().limit(4).forEach(area -> gaps.add(item(area, "GAP", 78, "Coverage", "MEDIUM")));
        }
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) {
            gaps.add(item("Weak Project Proof", "GAP", 76, "Proof", "MEDIUM"));
        }
        if (coherence.getProjectProofScore() != null && coherence.getProjectProofScore() < 50) {
            gaps.add(item("Portfolio Proof Alignment", "GAP", 70, "Coherence", "MEDIUM"));
        }

        strengths.sort(Comparator.comparing(StrengthGapItemDto::getScore).reversed());
        gaps.sort(Comparator.comparing(StrengthGapItemDto::getScore).reversed());

        List<StrengthGapItemDto> topStrengths = strengths.stream().distinct().limit(3).toList();
        List<StrengthGapItemDto> topGaps = gaps.stream().distinct().limit(3).toList();

        return StrengthsGapsDto.builder()
                .dominantFamily(family)
                .summary(buildSummary(family, topStrengths, topGaps, proof))
                .strengths(topStrengths)
                .gaps(topGaps)
                .recommendations(buildRecommendations(topGaps))
                .build();
    }

    private StrengthGapItemDto item(String label, String type, Integer score, String category, String priority) {
        return StrengthGapItemDto.builder().label(label).type(type).score(score).category(category).priority(priority).build();
    }

    private String buildSummary(DeveloperFamily family, List<StrengthGapItemDto> strengths, List<StrengthGapItemDto> gaps, ProjectProofStrengthDto proof) {
        String strengthPart = strengths.isEmpty() ? "a few early signals" : strengths.stream().map(StrengthGapItemDto::getLabel).limit(2).reduce((a, b) -> a + " and " + b).orElse("a few early signals");
        String gapPart = gaps.isEmpty() ? "no major missing layers" : gaps.stream().map(StrengthGapItemDto::getLabel).limit(2).reduce((a, b) -> a + " and " + b).orElse("no major missing layers");
        if (proof.getStrongProjects() != null && proof.getStrongProjects() > 0) {
            return "Your portfolio is strongest in " + strengthPart + ". The main gaps are " + gapPart + ". The dominant " + PortfolioAiSupport.formatFamilyLabel(family).toLowerCase() + " direction is visible and supported by project proof.";
        }
        return "Your portfolio is strongest in " + strengthPart + ". The main gaps are " + gapPart + ". However, the project layer still needs to support this direction more clearly.";
    }

    private List<String> buildRecommendations(List<StrengthGapItemDto> gaps) {
        List<String> recommendations = new ArrayList<>();
        for (StrengthGapItemDto gap : gaps) {
            switch (gap.getLabel()) {
                case "Testing", "Testing & Quality" -> recommendations.add("Strengthen your dominant field with stronger testing and quality proof.");
                case "Application Security" -> recommendations.add("Add security depth inside your main specialization before expanding outward.");
                case "Containerization", "CI/CD", "Cloud / Deployment", "Deployment & DevOps" -> recommendations.add("Add adjacent delivery and deployment layers after strengthening the core specialization.");
                case "Monitoring", "Observability & Monitoring" -> recommendations.add("Improve operational maturity with observability and monitoring proof.");
                case "Weak Project Proof", "Portfolio Proof Alignment" -> recommendations.add("Strengthen project descriptions, links, media, and implementation proof so your portfolio supports its main direction.");
                default -> recommendations.add("Close the most important missing layers in your dominant direction first.");
            }
        }
        if (recommendations.isEmpty()) recommendations.add("Continue deepening your strongest area before moving into optional expansion.");
        return recommendations.stream().distinct().toList();
    }
}
