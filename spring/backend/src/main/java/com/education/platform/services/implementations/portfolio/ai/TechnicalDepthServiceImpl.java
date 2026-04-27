package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.TechnicalDepthDto;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.ai.TechnicalDepthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class TechnicalDepthServiceImpl extends AbstractPortfolioAiService implements TechnicalDepthService {

    public TechnicalDepthServiceImpl(CurrentUserService currentUserService, PortfolioRepository portfolioRepository) {
        super(currentUserService, portfolioRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicalDepthDto analyzeCurrentUser() {
        Portfolio portfolio = getCurrentUserPortfolio();
        DeveloperFamily family = PortfolioAiSupport.detectDominantFamily(portfolio);
        List<String> skillNames = PortfolioAiSupport.skillNames(portfolio.getSkills());
        Set<SkillCategory> categories = PortfolioAiSupport.skillCategories(portfolio.getSkills());
        List<PortfolioProject> projects = PortfolioAiSupport.sortedProjects(portfolio);

        if (family == DeveloperFamily.GENERAL || categories.isEmpty()) {
            return TechnicalDepthDto.builder()
                    .dominantFamily(DeveloperFamily.GENERAL)
                    .foundationalScore(0)
                    .advancedScore(0)
                    .projectDepthScore(0)
                    .depthScore(0)
                    .level("UNDEFINED")
                    .strongSignals(List.of())
                    .missingDepthAreas(List.of())
                    .recommendations(List.of(
                            "Add more clearly categorized technical skills.",
                            "Add projects that prove one stronger specialization.",
                            "Use your title and bio to make the main technical direction explicit."
                    ))
                    .build();
        }

        int foundationalScore = family == DeveloperFamily.BACKEND
                ? calculateBackendFoundationScore(categories, skillNames)
                : calculateWeightedCategoryScore(categories, foundationalCategories(family), 40);
        int advancedScore = family == DeveloperFamily.BACKEND
                ? calculateBackendAdvancedScore(categories, skillNames)
                : calculateWeightedCategoryScore(categories, advancedCategories(family), 35);
        int projectDepthScore = family == DeveloperFamily.BACKEND
                ? calculateBackendProjectDepthScore(projects)
                : calculateGenericProjectDepthScore(projects, family);
        int depthScore = Math.min(100, foundationalScore + advancedScore + projectDepthScore);

        List<String> strongSignals = buildStrongSignals(family, categories, skillNames, projects, foundationalScore, advancedScore, projectDepthScore);
        List<String> missingDepthAreas = family == DeveloperFamily.BACKEND
                ? buildBackendMissingDepthAreas(categories, skillNames)
                : buildGenericMissingDepthAreas(categories, advancedCategories(family));

        return TechnicalDepthDto.builder()
                .dominantFamily(family)
                .foundationalScore(foundationalScore)
                .advancedScore(advancedScore)
                .projectDepthScore(projectDepthScore)
                .depthScore(depthScore)
                .level(depthLevel(depthScore))
                .strongSignals(strongSignals)
                .missingDepthAreas(missingDepthAreas)
                .recommendations(buildRecommendations(family, projectDepthScore, missingDepthAreas))
                .build();
    }

    private int calculateBackendFoundationScore(Set<SkillCategory> categories, List<String> skillNames) {
        int score = 0;
        if (categories.contains(SkillCategory.BACKEND_DEVELOPMENT)) score += 12;
        if (categories.contains(SkillCategory.SQL_DATABASES)) score += 8;

        int foundationKeywordMatches = PortfolioAiSupport.countKeywordMatches(skillNames, PortfolioAiSupport.BACKEND_FOUNDATION_KEYWORDS);
        if (foundationKeywordMatches >= 5) score += 10;
        else if (foundationKeywordMatches >= 3) score += 7;
        else if (foundationKeywordMatches >= 1) score += 4;

        long coreBackendSkills = skillNames.stream()
                .filter(skill -> skill.contains("java")
                        || skill.contains("spring")
                        || skill.contains("api")
                        || skill.contains("sql")
                        || skill.contains("mysql")
                        || skill.contains("postgres")
                        || skill.contains("jpa")
                        || skill.contains("hibernate"))
                .count();
        if (coreBackendSkills >= 4) score += 6;
        else if (coreBackendSkills >= 2) score += 4;
        else if (coreBackendSkills >= 1) score += 2;

        return Math.min(40, score);
    }

    private int calculateBackendAdvancedScore(Set<SkillCategory> categories, List<String> skillNames) {
        int score = 0;
        if (categories.contains(SkillCategory.APPLICATION_SECURITY)
                || categories.contains(SkillCategory.IDENTITY_ACCESS_MANAGEMENT)
                || PortfolioAiSupport.containsAny(skillNames, Set.of("jwt", "oauth", "oauth2", "spring security", "security"))) {
            score += 8;
        }
        if (categories.contains(SkillCategory.CONTAINERIZATION)
                || categories.contains(SkillCategory.CI_CD)
                || categories.contains(SkillCategory.CLOUD_COMPUTING)
                || PortfolioAiSupport.containsAny(skillNames, Set.of("docker", "kubernetes", "github actions", "aws", "cloud"))) {
            score += 8;
        }
        if (categories.contains(SkillCategory.MONITORING)
                || PortfolioAiSupport.containsAny(skillNames, Set.of("monitoring", "prometheus", "grafana"))) {
            score += 5;
        }
        if (categories.contains(SkillCategory.NOSQL_DATABASES)
                || PortfolioAiSupport.containsAny(skillNames, Set.of("mongodb", "redis", "kafka"))) {
            score += 7;
        }
        if (categories.contains(SkillCategory.QUALITY_ASSURANCE)
                || PortfolioAiSupport.containsAny(skillNames, Set.of("testing", "junit", "mockito", "microservices"))) {
            score += 7;
        }
        return Math.min(35, score);
    }

    private int calculateBackendProjectDepthScore(List<PortfolioProject> projects) {
        List<Integer> scores = new ArrayList<>();
        for (PortfolioProject project : projects) {
            if (!PortfolioAiSupport.projectMatchesFamily(project, DeveloperFamily.BACKEND)) {
                continue;
            }
            int score = 5;
            List<String> projectSkillNames = PortfolioAiSupport.skillNames(project.getSkills());
            String text = PortfolioAiSupport.projectTextBlob(project);

            int foundationMatches = PortfolioAiSupport.countKeywordMatches(projectSkillNames, PortfolioAiSupport.BACKEND_FOUNDATION_KEYWORDS)
                    + PortfolioAiSupport.countKeywordMatchesInText(text, PortfolioAiSupport.BACKEND_FOUNDATION_KEYWORDS);
            if (foundationMatches >= 4) score += 7;
            else if (foundationMatches >= 2) score += 5;
            else if (foundationMatches >= 1) score += 3;

            int advancedMatches = PortfolioAiSupport.countKeywordMatches(projectSkillNames, PortfolioAiSupport.BACKEND_ADVANCED_KEYWORDS)
                    + PortfolioAiSupport.countKeywordMatchesInText(text, PortfolioAiSupport.BACKEND_ADVANCED_KEYWORDS);
            if (advancedMatches >= 3) score += 8;
            else if (advancedMatches >= 1) score += 5;

            if (PortfolioAiSupport.hasText(project.getProjectUrl())) score += 2;
            if (project.getMedia() != null && !project.getMedia().isEmpty()) score += 1;
            if (PortfolioAiSupport.hasText(project.getDescription()) && project.getDescription().trim().length() >= 80) score += 2;

            scores.add(Math.min(25, score));
        }
        if (scores.isEmpty()) return 0;
        scores.sort(java.util.Comparator.reverseOrder());
        if (scores.size() == 1) return scores.get(0);
        return Math.min(25, (int) Math.round((scores.get(0) * 0.7) + (scores.get(1) * 0.3)));
    }

    private int calculateGenericProjectDepthScore(List<PortfolioProject> projects, DeveloperFamily family) {
        List<Integer> scores = new ArrayList<>();
        for (PortfolioProject project : projects) {
            if (!PortfolioAiSupport.projectMatchesFamily(project, family)) {
                continue;
            }
            int score = 8;
            int skillsCount = project.getSkills() == null ? 0 : project.getSkills().size();
            if (skillsCount >= 4) score += 8;
            else if (skillsCount >= 2) score += 5;
            else if (skillsCount == 1) score += 2;

            String blob = PortfolioAiSupport.projectTextBlob(project);
            if (PortfolioAiSupport.familyKeywords(family).stream().anyMatch(blob::contains)) score += 5;
            if (PortfolioAiSupport.hasText(project.getProjectUrl())) score += 4;
            scores.add(Math.min(25, score));
        }
        if (scores.isEmpty()) return 0;
        return (int) Math.round(scores.stream().mapToInt(Integer::intValue).average().orElse(0));
    }

    private int calculateWeightedCategoryScore(Set<SkillCategory> actualCategories, Set<SkillCategory> expectedCategories, int maxScore) {
        if (expectedCategories.isEmpty()) return 0;
        long matched = expectedCategories.stream().filter(actualCategories::contains).count();
        return (int) Math.round(((double) matched / expectedCategories.size()) * maxScore);
    }

    private List<String> buildStrongSignals(DeveloperFamily family, Set<SkillCategory> actualCategories, List<String> skillNames,
                                            List<PortfolioProject> projects, int foundationalScore, int advancedScore, int projectDepthScore) {
        List<String> signals = new ArrayList<>();
        if (family == DeveloperFamily.BACKEND) {
            if (actualCategories.contains(SkillCategory.BACKEND_DEVELOPMENT)) signals.add("Clear backend development foundation is present.");
            if (actualCategories.contains(SkillCategory.SQL_DATABASES) || PortfolioAiSupport.containsAny(skillNames, Set.of("mysql", "postgresql", "sql"))) {
                signals.add("Relational database knowledge is visible.");
            }
            if (PortfolioAiSupport.containsAny(skillNames, Set.of("java", "spring", "spring boot"))) {
                signals.add("Core backend stack signals are visible through named technologies.");
            }
            if (advancedScore >= 10) signals.add("Some advanced backend layers are already emerging.");
            if (projectDepthScore >= 10) {
                long matchingProjects = projects.stream().filter(project -> PortfolioAiSupport.projectMatchesFamily(project, DeveloperFamily.BACKEND)).count();
                if (matchingProjects > 0) signals.add(matchingProjects + " backend-oriented project(s) provide technical proof.");
            }
        } else {
            if (foundationalScore > 0) signals.add("Strong foundational signal in " + PortfolioAiSupport.formatFamilyLabel(family).toLowerCase() + ".");
            if (advancedScore > 0) signals.add("Advanced layers are beginning to appear.");
            if (projectDepthScore > 0) signals.add("Projects provide technical proof in this specialization.");
        }
        return signals.stream().distinct().toList();
    }

    private List<String> buildBackendMissingDepthAreas(Set<SkillCategory> categories, List<String> skillNames) {
        List<String> missing = new ArrayList<>();
        if (!categories.contains(SkillCategory.CONTAINERIZATION) && !PortfolioAiSupport.containsAny(skillNames, Set.of("docker", "kubernetes"))) missing.add("Containerization");
        if (!categories.contains(SkillCategory.CI_CD) && !PortfolioAiSupport.containsAny(skillNames, Set.of("ci/cd", "github actions"))) missing.add("CI/CD");
        if (!categories.contains(SkillCategory.APPLICATION_SECURITY) && !PortfolioAiSupport.containsAny(skillNames, Set.of("jwt", "oauth", "spring security", "security"))) missing.add("Application Security");
        if (!categories.contains(SkillCategory.MONITORING) && !PortfolioAiSupport.containsAny(skillNames, Set.of("monitoring", "prometheus", "grafana"))) missing.add("Monitoring");
        if (!categories.contains(SkillCategory.NOSQL_DATABASES) && !PortfolioAiSupport.containsAny(skillNames, Set.of("mongodb", "redis"))) missing.add("NoSQL / Caching Layer");
        if (!categories.contains(SkillCategory.CLOUD_COMPUTING) && !PortfolioAiSupport.containsAny(skillNames, Set.of("aws", "cloud"))) missing.add("Cloud / Deployment");
        if (!categories.contains(SkillCategory.QUALITY_ASSURANCE) && !PortfolioAiSupport.containsAny(skillNames, Set.of("testing", "junit", "mockito"))) missing.add("Testing");
        return missing;
    }

    private List<String> buildGenericMissingDepthAreas(Set<SkillCategory> actualCategories, Set<SkillCategory> advancedCategories) {
        return advancedCategories.stream()
                .filter(category -> !actualCategories.contains(category))
                .map(PortfolioAiSupport::formatCategoryLabel)
                .toList();
    }

    private List<String> buildRecommendations(DeveloperFamily family, int projectDepthScore, List<String> missingDepthAreas) {
        List<String> recommendations = new ArrayList<>();
        if (family == DeveloperFamily.BACKEND) {
            if (projectDepthScore < 10) recommendations.add("Add at least one backend project that clearly proves implementation depth, not only stack familiarity.");
            if (missingDepthAreas.contains("Application Security")) recommendations.add("Strengthen backend depth with authentication and authorization proof such as JWT, Spring Security, or OAuth2.");
            if (missingDepthAreas.contains("Containerization") || missingDepthAreas.contains("Cloud / Deployment")) recommendations.add("Add deployment-oriented proof through Docker and a visible cloud or hosting workflow.");
            if (missingDepthAreas.contains("Testing")) recommendations.add("Add testing depth with JUnit or Mockito to show quality engineering around your backend stack.");
            if (missingDepthAreas.contains("Monitoring")) recommendations.add("Improve operational depth with logging, monitoring, or observability tooling.");
        } else {
            if (!missingDepthAreas.isEmpty()) recommendations.add("Strengthen your specialization with more advanced layers around your core stack.");
            if (projectDepthScore < 12) recommendations.add("Add or improve projects that clearly demonstrate deeper technical work.");
        }
        if (recommendations.isEmpty()) recommendations.add("Continue deepening your specialization with stronger advanced proof across projects.");
        return recommendations.stream().distinct().toList();
    }

    private String depthLevel(int score) {
        if (score >= 85) return "SPECIALIZED";
        if (score >= 60) return "SOLID";
        if (score >= 30) return "EMERGING";
        if (score >= 1) return "FOUNDATIONAL";
        return "UNDEFINED";
    }

    private Set<SkillCategory> foundationalCategories(DeveloperFamily family) {
        return switch (family) {
            case BACKEND -> EnumSet.of(SkillCategory.BACKEND_DEVELOPMENT, SkillCategory.SQL_DATABASES);
            case FRONTEND -> EnumSet.of(SkillCategory.FRONTEND_DEVELOPMENT, SkillCategory.UI_UX_DESIGN);
            case FULL_STACK -> EnumSet.of(SkillCategory.FULL_STACK_DEVELOPMENT, SkillCategory.FRONTEND_DEVELOPMENT, SkillCategory.BACKEND_DEVELOPMENT);
            case DEVOPS_CLOUD -> EnumSet.of(SkillCategory.DEVOPS, SkillCategory.CLOUD_COMPUTING, SkillCategory.CONTAINERIZATION);
            case DATA_AI -> EnumSet.of(SkillCategory.DATA_ANALYSIS, SkillCategory.MACHINE_LEARNING, SkillCategory.DATA_ENGINEERING);
            case SECURITY -> EnumSet.of(SkillCategory.APPLICATION_SECURITY, SkillCategory.NETWORK_SECURITY);
            case DESIGN_CREATIVE -> EnumSet.of(SkillCategory.UI_UX_DESIGN, SkillCategory.GRAPHIC_DESIGN);
            case GENERAL -> EnumSet.noneOf(SkillCategory.class);
        };
    }

    private Set<SkillCategory> advancedCategories(DeveloperFamily family) {
        return switch (family) {
            case BACKEND -> EnumSet.of(SkillCategory.NOSQL_DATABASES, SkillCategory.DATABASE_ADMINISTRATION, SkillCategory.APPLICATION_SECURITY, SkillCategory.IDENTITY_ACCESS_MANAGEMENT, SkillCategory.CONTAINERIZATION, SkillCategory.CI_CD, SkillCategory.CLOUD_COMPUTING, SkillCategory.MONITORING, SkillCategory.QUALITY_ASSURANCE);
            case FRONTEND -> EnumSet.of(SkillCategory.QUALITY_ASSURANCE, SkillCategory.CONTENT_STRATEGY, SkillCategory.TECHNICAL_WRITING, SkillCategory.COPYWRITING, SkillCategory.E_COMMERCE, SkillCategory.DIGITAL_MARKETING, SkillCategory.SEO_STRATEGY);
            case FULL_STACK -> EnumSet.of(SkillCategory.SQL_DATABASES, SkillCategory.NOSQL_DATABASES, SkillCategory.APPLICATION_SECURITY, SkillCategory.CONTAINERIZATION, SkillCategory.CI_CD, SkillCategory.MONITORING, SkillCategory.QUALITY_ASSURANCE);
            case DEVOPS_CLOUD -> EnumSet.of(SkillCategory.INFRASTRUCTURE_AS_CODE, SkillCategory.CI_CD, SkillCategory.MONITORING, SkillCategory.DEVSECOPS, SkillCategory.SECURITY_OPERATIONS, SkillCategory.NETWORK_SECURITY);
            case DATA_AI -> EnumSet.of(SkillCategory.DEEP_LEARNING, SkillCategory.NATURAL_LANGUAGE_PROCESSING, SkillCategory.COMPUTER_VISION, SkillCategory.LLM_ENGINEERING, SkillCategory.SQL_DATABASES, SkillCategory.NOSQL_DATABASES, SkillCategory.CLOUD_COMPUTING);
            case SECURITY -> EnumSet.of(SkillCategory.IDENTITY_ACCESS_MANAGEMENT, SkillCategory.SECURITY_OPERATIONS, SkillCategory.DEVSECOPS, SkillCategory.CI_CD, SkillCategory.CLOUD_COMPUTING, SkillCategory.MONITORING);
            case DESIGN_CREATIVE -> EnumSet.of(SkillCategory.ILLUSTRATION, SkillCategory.MOTION_GRAPHICS, SkillCategory.VIDEO_EDITING, SkillCategory.THREE_D_MODELING, SkillCategory.CONTENT_WRITING, SkillCategory.CONTENT_STRATEGY);
            case GENERAL -> EnumSet.noneOf(SkillCategory.class);
        };
    }
}
