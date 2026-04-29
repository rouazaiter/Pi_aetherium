package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.NextBestMovesDto;
import com.education.platform.dto.portfolio.ai.PortfolioDnaSummaryDto;
import com.education.platform.dto.portfolio.ai.ProfileCoherenceDto;
import com.education.platform.dto.portfolio.ai.ProjectProofStrengthDto;
import com.education.platform.dto.portfolio.ai.SkillCoverageDto;
import com.education.platform.dto.portfolio.ai.StrengthsGapsDto;
import com.education.platform.dto.portfolio.ai.TechnicalDepthDto;
import com.education.platform.services.interfaces.portfolio.ai.NextBestMovesService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioDnaSummaryService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioCoherenceService;
import com.education.platform.services.interfaces.portfolio.ai.ProjectProofStrengthService;
import com.education.platform.services.interfaces.portfolio.ai.SkillCoverageService;
import com.education.platform.services.interfaces.portfolio.ai.StrengthsGapsService;
import com.education.platform.services.interfaces.portfolio.ai.TechnicalDepthService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioDnaSummaryServiceImpl implements PortfolioDnaSummaryService {

    private final TechnicalDepthService technicalDepthService;
    private final SkillCoverageService skillCoverageService;
    private final ProjectProofStrengthService projectProofStrengthService;
    private final PortfolioCoherenceService portfolioCoherenceService;
    private final StrengthsGapsService strengthsGapsService;
    private final NextBestMovesService nextBestMovesService;

    public PortfolioDnaSummaryServiceImpl(
            TechnicalDepthService technicalDepthService,
            SkillCoverageService skillCoverageService,
            ProjectProofStrengthService projectProofStrengthService,
            PortfolioCoherenceService portfolioCoherenceService,
            StrengthsGapsService strengthsGapsService,
            NextBestMovesService nextBestMovesService) {
        this.technicalDepthService = technicalDepthService;
        this.skillCoverageService = skillCoverageService;
        this.projectProofStrengthService = projectProofStrengthService;
        this.portfolioCoherenceService = portfolioCoherenceService;
        this.strengthsGapsService = strengthsGapsService;
        this.nextBestMovesService = nextBestMovesService;
    }

    @Override
    public PortfolioDnaSummaryDto analyzeCurrentUser() {
        TechnicalDepthDto depth = technicalDepthService.analyzeCurrentUser();
        SkillCoverageDto coverage = skillCoverageService.analyzeCurrentUser();
        ProjectProofStrengthDto proof = projectProofStrengthService.analyzeCurrentUser();
        ProfileCoherenceDto coherence = portfolioCoherenceService.analyzeCurrentUser();
        StrengthsGapsDto strengthsGaps = strengthsGapsService.analyzeCurrentUser();
        NextBestMovesDto nextMoves = nextBestMovesService.analyzeCurrentUser();

        int strengthsContribution = calculateStrengthGapContribution(strengthsGaps);
        int score = clamp((int) Math.round(
                value(depth.getDepthScore()) * 0.30
                        + value(coverage.getCoverageScore()) * 0.20
                        + value(proof.getProofScore()) * 0.20
                        + value(coherence.getTotalScore()) * 0.15
                        + strengthsContribution * 0.15
        ));

        DeveloperFamily family = depth.getDominantFamily() == null ? DeveloperFamily.GENERAL : depth.getDominantFamily();

        return PortfolioDnaSummaryDto.builder()
                .dominantFamily(family)
                .dnaType(buildDnaType(family, coherence, score))
                .maturityLevel(maturityLevel(score))
                .profileStrengthScore(score)
                .marketReadiness(buildMarketReadiness(depth, coverage, proof, nextMoves))
                .strongestSignals(strengthsGaps.getStrengths() == null ? List.of() : strengthsGaps.getStrengths().stream().map(item -> item.getLabel()).limit(3).toList())
                .mainWeakPoints(strengthsGaps.getGaps() == null ? List.of() : strengthsGaps.getGaps().stream().map(item -> item.getLabel()).limit(3).toList())
                .strategicPriorities(nextMoves.getCoreMoves() == null ? List.of() : nextMoves.getCoreMoves().stream().map(item -> item.getLabel()).limit(3).toList())
                .build();
    }

    private int calculateStrengthGapContribution(StrengthsGapsDto dto) {
        int strengthCount = dto.getStrengths() == null ? 0 : dto.getStrengths().size();
        int gapCount = dto.getGaps() == null ? 0 : dto.getGaps().size();
        return clamp(50 + (strengthCount * 10) - (gapCount * 8));
    }

    private String maturityLevel(int score) {
        if (score >= 85) return "SPECIALIZED";
        if (score >= 65) return "STRONG";
        if (score >= 40) return "GROWING";
        return "EARLY";
    }

    private String buildDnaType(DeveloperFamily family, ProfileCoherenceDto coherence, int score) {
        if (family == DeveloperFamily.BACKEND) return score >= 75 ? "Backend Specialist" : "Growing Backend Profile";
        if (family == DeveloperFamily.FRONTEND) return score >= 75 ? "Frontend Specialist" : "Growing Frontend Profile";
        if (family == DeveloperFamily.FULL_STACK) return score >= 75 ? "Full Stack Developer" : "Growing Full Stack Profile";
        if (family == DeveloperFamily.DEVOPS_CLOUD) return score >= 75 ? "DevOps / Cloud Specialist" : "Growing DevOps Profile";
        if (family == DeveloperFamily.DATA_AI) return score >= 75 ? "Data / AI Specialist" : "Growing Data / AI Profile";
        if (family == DeveloperFamily.SECURITY) return score >= 75 ? "Security Specialist" : "Growing Security Profile";
        if (family == DeveloperFamily.DESIGN_CREATIVE) return score >= 75 ? "Creative Specialist" : "Growing Creative Profile";
        if (Boolean.TRUE.equals(coherence.getMismatchDetected())) return "Mixed Positioning";
        return "Emerging Profile";
    }

    private String buildMarketReadiness(TechnicalDepthDto depth, SkillCoverageDto coverage, ProjectProofStrengthDto proof, NextBestMovesDto nextMoves) {
        if (value(depth.getDepthScore()) >= 75 && value(coverage.getCoverageScore()) >= 80 && value(proof.getProofScore()) >= 70) {
            return "Strong";
        }
        if (value(proof.getProofScore()) < 45) {
            return "Needs stronger project proof";
        }
        if (nextMoves.getCoreMoves() != null && !nextMoves.getCoreMoves().isEmpty()) {
            return "Promising but still missing core improvements";
        }
        return "Moderate";
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int value(Integer number) {
        return number == null ? 0 : number;
    }
}
