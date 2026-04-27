package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.NextBestMoveItemDto;
import com.education.platform.dto.portfolio.ai.NextBestMovesDto;
import com.education.platform.dto.portfolio.ai.ProfileCoherenceDto;
import com.education.platform.dto.portfolio.ai.ProjectProofStrengthDto;
import com.education.platform.dto.portfolio.ai.SkillCoverageDto;
import com.education.platform.dto.portfolio.ai.TechnicalDepthDto;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.ai.NextBestMovesService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioCoherenceService;
import com.education.platform.services.interfaces.portfolio.ai.ProjectProofStrengthService;
import com.education.platform.services.interfaces.portfolio.ai.SkillCoverageService;
import com.education.platform.services.interfaces.portfolio.ai.TechnicalDepthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NextBestMovesServiceImpl extends AbstractPortfolioAiService implements NextBestMovesService {

    private final TechnicalDepthService technicalDepthService;
    private final SkillCoverageService skillCoverageService;
    private final ProjectProofStrengthService projectProofStrengthService;
    private final PortfolioCoherenceService portfolioCoherenceService;

    public NextBestMovesServiceImpl(
            CurrentUserService currentUserService,
            PortfolioRepository portfolioRepository,
            TechnicalDepthService technicalDepthService,
            SkillCoverageService skillCoverageService,
            ProjectProofStrengthService projectProofStrengthService,
            PortfolioCoherenceService portfolioCoherenceService) {
        super(currentUserService, portfolioRepository);
        this.technicalDepthService = technicalDepthService;
        this.skillCoverageService = skillCoverageService;
        this.projectProofStrengthService = projectProofStrengthService;
        this.portfolioCoherenceService = portfolioCoherenceService;
    }

    @Override
    @Transactional(readOnly = true)
    public NextBestMovesDto analyzeCurrentUser() {
        Portfolio portfolio = getCurrentUserPortfolio();
        TechnicalDepthDto depth = technicalDepthService.analyzeCurrentUser();
        SkillCoverageDto coverage = skillCoverageService.analyzeCurrentUser();
        ProjectProofStrengthDto proof = projectProofStrengthService.analyzeCurrentUser();
        ProfileCoherenceDto coherence = portfolioCoherenceService.analyzeCurrentUser();
        DeveloperFamily family = depth.getDominantFamily() == null ? DeveloperFamily.GENERAL : depth.getDominantFamily();

        List<NextBestMoveItemDto> core = new ArrayList<>();
        List<NextBestMoveItemDto> adjacent = new ArrayList<>();
        List<NextBestMoveItemDto> expansion = new ArrayList<>();

        if (Boolean.TRUE.equals(coherence.getMismatchDetected())) {
            String familyLabel = PortfolioAiSupport.formatFamilyLabel(family);
            core.add(move("Choose one clear " + familyLabel + " direction", "CORE", 98, "Direction", "Your written positioning and technical evidence are not aligned yet."));
            core.add(move("Rewrite the title and bio to match your strongest evidence", "CORE", 94, "Positioning", "The portfolio becomes stronger when title, bio, skills, and projects reinforce the same direction."));
            adjacent.add(move("Reduce identity mismatch between written positioning and project proof", "ADJACENT", 84, "Coherence", "Projects should clearly support the same story as the profile text."));
        } else {
            switch (family) {
                case BACKEND -> buildBackendMoves(core, adjacent, expansion, depth, coverage, proof, coherence);
                case FRONTEND -> buildFrontendMoves(core, adjacent, expansion, coverage, proof, coherence);
                case FULL_STACK -> buildFullStackMoves(core, adjacent, expansion, coverage, proof, depth);
                case DEVOPS_CLOUD -> buildDevopsMoves(core, adjacent, expansion, depth, proof);
                case DATA_AI -> buildDataAiMoves(core, adjacent, expansion, coverage, proof, depth);
                case SECURITY -> buildSecurityMoves(core, adjacent, expansion, coverage, proof, depth);
                case DESIGN_CREATIVE -> buildDesignMoves(core, adjacent, expansion, coverage, proof, depth);
                case GENERAL -> core.add(move("Clarify the dominant direction", "CORE", 90, "Identity", "The portfolio needs a clearer main direction before broader optimization becomes useful."));
            }
        }

        List<NextBestMoveItemDto> coreMoves = core.stream().sorted(Comparator.comparing(NextBestMoveItemDto::getPriority).reversed()).limit(3).toList();
        List<NextBestMoveItemDto> adjacentMoves = adjacent.stream().sorted(Comparator.comparing(NextBestMoveItemDto::getPriority).reversed()).limit(3).toList();
        List<NextBestMoveItemDto> expansionMoves = expansion.stream().sorted(Comparator.comparing(NextBestMoveItemDto::getPriority).reversed()).limit(2).toList();

        return NextBestMovesDto.builder()
                .dominantFamily(family)
                .summary(buildSummary(family, coreMoves, adjacentMoves, expansionMoves, coherence))
                .coreMoves(coreMoves)
                .adjacentMoves(adjacentMoves)
                .expansionMoves(expansionMoves)
                .build();
    }

    private void buildBackendMoves(List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent, List<NextBestMoveItemDto> expansion,
                                   TechnicalDepthDto depth, SkillCoverageDto coverage, ProjectProofStrengthDto proof, ProfileCoherenceDto coherence) {
        if (contains(depth.getMissingDepthAreas(), "Testing") || contains(coverage.getMissingAreas(), "Testing & Quality")) {
            core.add(move("Add backend testing proof", "CORE", 95, "Testing", "Strengthen backend credibility with JUnit, Mockito, or test-backed services."));
        }
        if (contains(depth.getMissingDepthAreas(), "Application Security") || contains(coverage.getMissingAreas(), "Security")) {
            core.add(move("Add authentication and authorization proof", "CORE", 93, "Security", "Security is part of strong backend credibility, not just an optional extra."));
        }
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) {
            core.add(move("Improve backend project implementation proof", "CORE", 90, "Proof", "The backend direction is stronger when projects clearly prove real implementation work."));
        }
        if (contains(depth.getMissingDepthAreas(), "Containerization")) {
            adjacent.add(move("Add Docker-based deployment proof", "ADJACENT", 85, "DevOps", "Docker is a high-value adjacent layer that strengthens backend employability."));
        }
        if (contains(depth.getMissingDepthAreas(), "CI/CD")) {
            adjacent.add(move("Add CI/CD workflow proof", "ADJACENT", 82, "DevOps", "CI/CD shows delivery maturity and strengthens production readiness."));
        }
        if (contains(depth.getMissingDepthAreas(), "Monitoring")) {
            adjacent.add(move("Add observability or monitoring proof", "ADJACENT", 78, "DevOps", "Monitoring improves operational maturity around the backend specialization."));
        }
        if (value(depth.getAdvancedScore()) >= 16 && value(depth.getProjectDepthScore()) >= 12) {
            expansion.add(move("Explore cloud-native backend architecture", "EXPANSION", 55, "Cloud", "The backend core is strong enough to expand toward more advanced cloud patterns."));
        }
    }

    private void buildFrontendMoves(List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent, List<NextBestMoveItemDto> expansion,
                                    SkillCoverageDto coverage, ProjectProofStrengthDto proof, ProfileCoherenceDto coherence) {
        if (contains(coverage.getMissingAreas(), "Testing & Quality")) core.add(move("Add frontend testing proof", "CORE", 95, "Testing", "Frontend portfolios become much stronger when UI work is backed by testing."));
        if (contains(coverage.getMissingAreas(), "Full Experience & Product")) core.add(move("Deepen product and interaction thinking", "CORE", 90, "Frontend", "Strong frontend portfolios should show more than UI construction."));
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) core.add(move("Improve frontend project proof", "CORE", 88, "Proof", "Projects should show implementation depth and polish more clearly."));
        adjacent.add(move("Add accessibility and performance signals", "ADJACENT", 80, "Frontend", "These are market-relevant supporting layers for strong frontend portfolios."));
        if (coherence.getProjectProofScore() != null && coherence.getProjectProofScore() < 60) adjacent.add(move("Make project proof more aligned with the frontend identity", "ADJACENT", 76, "Coherence", "Projects should reinforce the frontend story more clearly."));
        if (proof.getStrongProjects() != null && proof.getStrongProjects() >= 2) expansion.add(move("Explore API integration and light full-stack depth", "EXPANSION", 50, "Full Stack", "Once the frontend core is credible, adjacent backend understanding becomes a useful expansion."));
    }

    private void buildFullStackMoves(List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent, List<NextBestMoveItemDto> expansion,
                                     SkillCoverageDto coverage, ProjectProofStrengthDto proof, TechnicalDepthDto depth) {
        if (contains(coverage.getMissingAreas(), "Backend Layer")) core.add(move("Strengthen the backend layer", "CORE", 94, "Backend", "A full-stack profile is weak if one side of the stack is underdeveloped."));
        if (contains(coverage.getMissingAreas(), "Frontend Layer")) core.add(move("Strengthen the frontend layer", "CORE", 94, "Frontend", "A full-stack profile should show credible frontend capability."));
        if (contains(coverage.getMissingAreas(), "Delivery & Reliability")) core.add(move("Add delivery and reliability proof", "CORE", 90, "DevOps", "Full-stack portfolios become stronger when they show deployment, testing, or monitoring maturity."));
        if (contains(coverage.getMissingAreas(), "Data Layer")) adjacent.add(move("Deepen the data layer", "ADJACENT", 82, "Data", "A stronger data layer improves the credibility of full-stack architecture."));
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) adjacent.add(move("Improve end-to-end project proof", "ADJACENT", 80, "Proof", "Full-stack credibility comes from coherent end-to-end projects."));
        if (value(depth.getDepthScore()) >= 70) expansion.add(move("Choose a deeper specialization lane", "EXPANSION", 55, "Career", "Once the full-stack core is solid, deeper specialization can make the profile more distinctive."));
    }

    private void buildDevopsMoves(List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent, List<NextBestMoveItemDto> expansion,
                                  TechnicalDepthDto depth, ProjectProofStrengthDto proof) {
        if (contains(depth.getMissingDepthAreas(), "Infrastructure As Code")) core.add(move("Add infrastructure-as-code proof", "CORE", 94, "IaC", "Infrastructure as code is central to DevOps maturity."));
        if (contains(depth.getMissingDepthAreas(), "Monitoring")) core.add(move("Add monitoring and observability proof", "CORE", 91, "Monitoring", "Observability is part of the core DevOps field."));
        if (contains(depth.getMissingDepthAreas(), "Application Security")) adjacent.add(move("Add DevSecOps or security hardening proof", "ADJACENT", 84, "Security", "Security is a highly relevant adjacent layer for DevOps profiles."));
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) core.add(move("Improve delivery-focused project proof", "CORE", 88, "Proof", "DevOps portfolios need visible automation, infrastructure, and delivery evidence."));
        if (value(depth.getAdvancedScore()) >= 16) expansion.add(move("Explore platform engineering direction", "EXPANSION", 52, "Platform", "Once the DevOps core is solid, platform engineering becomes a natural expansion path."));
    }

    private void buildDataAiMoves(List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent, List<NextBestMoveItemDto> expansion,
                                  SkillCoverageDto coverage, ProjectProofStrengthDto proof, TechnicalDepthDto depth) {
        if (contains(coverage.getMissingAreas(), "Modeling & Learning")) core.add(move("Strengthen modeling and machine learning depth", "CORE", 95, "AI", "A strong data or AI profile needs visible modeling depth."));
        if (contains(coverage.getMissingAreas(), "Data Foundations")) core.add(move("Strengthen data foundations", "CORE", 92, "Data", "The profile becomes more credible when data foundations are clearly visible."));
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) core.add(move("Add stronger applied data or AI project proof", "CORE", 90, "Proof", "Data and AI profiles become credible when projects demonstrate real use cases."));
        if (contains(coverage.getMissingAreas(), "Deployment & Production Readiness")) adjacent.add(move("Add production readiness or deployment proof", "ADJACENT", 84, "MLOps", "Production readiness is a valuable adjacent layer for data and AI portfolios."));
        if (contains(coverage.getMissingAreas(), "Data Storage & Access")) adjacent.add(move("Strengthen data storage and access patterns", "ADJACENT", 80, "Data", "Better storage and retrieval depth improves real-world credibility."));
        if (value(depth.getAdvancedScore()) >= 16) expansion.add(move("Explore MLOps or AI platform engineering", "EXPANSION", 55, "Platform", "Once the core AI profile is strong, MLOps becomes a natural expansion path."));
    }

    private void buildSecurityMoves(List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent, List<NextBestMoveItemDto> expansion,
                                    SkillCoverageDto coverage, ProjectProofStrengthDto proof, TechnicalDepthDto depth) {
        if (contains(coverage.getMissingAreas(), "Identity & Access")) core.add(move("Add IAM and access control proof", "CORE", 95, "Security", "Identity and access management is one of the most important core layers in security portfolios."));
        if (contains(coverage.getMissingAreas(), "Security Operations")) core.add(move("Strengthen security operations proof", "CORE", 92, "Security Operations", "A serious security profile needs visible monitoring, incident, or operational security depth."));
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) core.add(move("Improve security project proof", "CORE", 90, "Proof", "Security portfolios need projects that clearly demonstrate protection or secure workflows."));
        if (contains(coverage.getMissingAreas(), "Secure Delivery")) adjacent.add(move("Add secure delivery or DevSecOps proof", "ADJACENT", 84, "DevSecOps", "Secure pipelines are a valuable adjacent layer for modern security profiles."));
        if (contains(coverage.getMissingAreas(), "Infrastructure Security")) adjacent.add(move("Add infrastructure or cloud security proof", "ADJACENT", 80, "Cloud Security", "Cloud and infrastructure hardening improve market relevance."));
        if (value(depth.getAdvancedScore()) >= 16) expansion.add(move("Explore security architecture or platform security", "EXPANSION", 55, "Architecture", "Once the core security profile is strong, architecture-level security becomes a strong expansion path."));
    }

    private void buildDesignMoves(List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent, List<NextBestMoveItemDto> expansion,
                                  SkillCoverageDto coverage, ProjectProofStrengthDto proof, TechnicalDepthDto depth) {
        if (contains(coverage.getMissingAreas(), "Visual Production")) core.add(move("Strengthen visual production depth", "CORE", 92, "Creative", "A strong creative portfolio should show more than static design foundations."));
        if (contains(coverage.getMissingAreas(), "Narrative & Communication")) core.add(move("Strengthen narrative and communication depth", "CORE", 90, "Communication", "Creative profiles become stronger when they show the ability to communicate ideas."));
        if (proof.getWeakProjects() != null && proof.getWeakProjects() > 0) core.add(move("Improve creative project proof", "CORE", 88, "Proof", "Creative portfolios need visible pieces that demonstrate execution and presentation quality."));
        if (contains(coverage.getMissingAreas(), "Audience & Product Reach")) adjacent.add(move("Add audience and content-strategy depth", "ADJACENT", 82, "Strategy", "Content strategy and audience thinking are valuable adjacent layers for creative professionals."));
        if (contains(coverage.getMissingAreas(), "Media Content")) adjacent.add(move("Expand media-content versatility", "ADJACENT", 78, "Media", "Broader media capability can strengthen employability without weakening the creative core."));
        if (value(depth.getAdvancedScore()) >= 16) expansion.add(move("Explore creative direction or product design leadership", "EXPANSION", 52, "Leadership", "Once the creative core is strong, direction and strategy become natural expansion paths."));
    }

    private NextBestMoveItemDto move(String label, String type, Integer priority, String category, String reason) {
        return NextBestMoveItemDto.builder().label(label).type(type).priority(priority).category(category).reason(reason).build();
    }

    private boolean contains(List<String> values, String expected) {
        return values != null && values.contains(expected);
    }

    private int value(Integer number) {
        return number == null ? 0 : number;
    }

    private String buildSummary(DeveloperFamily family, List<NextBestMoveItemDto> core, List<NextBestMoveItemDto> adjacent,
                                List<NextBestMoveItemDto> expansion, ProfileCoherenceDto coherence) {
        String familyLabel = PortfolioAiSupport.formatFamilyLabel(family).toLowerCase();
        if (Boolean.TRUE.equals(coherence.getMismatchDetected())) {
            return "Your portfolio currently mixes signals. Choose one clearer direction, then align your title, bio, skills, and projects around it.";
        }
        if (!core.isEmpty()) {
            return "Your next best moves should first strengthen your main " + familyLabel + " direction. Only after the core becomes more credible should you focus on adjacent layers or optional expansion.";
        }
        if (!adjacent.isEmpty()) {
            return "Your dominant " + familyLabel + " direction is already relatively solid. The next improvements should come from adjacent layers that increase market readiness.";
        }
        return "Your dominant " + familyLabel + " direction already looks strong. The next steps can focus on optional expansion without weakening the core identity.";
    }
}
