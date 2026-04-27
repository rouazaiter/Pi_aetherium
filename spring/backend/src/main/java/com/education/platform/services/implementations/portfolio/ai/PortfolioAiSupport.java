package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class PortfolioAiSupport {

    static final Set<String> BACKEND_FOUNDATION_KEYWORDS = Set.of(
            "java", "spring", "spring boot", "backend", "api", "rest", "rest api",
            "sql", "mysql", "postgresql", "postgres", "jpa", "hibernate"
    );

    static final Set<String> BACKEND_ADVANCED_KEYWORDS = Set.of(
            "docker", "kubernetes", "jwt", "oauth", "oauth2", "spring security",
            "security", "redis", "mongodb", "kafka", "rabbitmq", "ci/cd",
            "github actions", "aws", "testing", "junit", "mockito", "microservices"
    );

    private PortfolioAiSupport() {
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    static String safeOwnerName(Portfolio portfolio) {
        if (portfolio != null && portfolio.getUser() != null && hasText(portfolio.getUser().getUsername())) {
            return portfolio.getUser().getUsername().trim();
        }
        return "You";
    }

    static List<String> skillNames(Collection<Skill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .map(Skill::getName)
                .filter(PortfolioAiSupport::hasText)
                .map(PortfolioAiSupport::normalize)
                .distinct()
                .toList();
    }

    static Set<SkillCategory> skillCategories(Collection<Skill> skills) {
        if (skills == null) {
            return Set.of();
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .map(Skill::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SkillCategory.class)));
    }

    static DeveloperFamily detectDominantFamily(Portfolio portfolio) {
        return SkillFamilyWeightMapper.dominantFamily(skillCategories(portfolio == null ? null : portfolio.getSkills()));
    }

    static boolean projectMatchesFamily(PortfolioProject project, DeveloperFamily family) {
        if (project == null || family == null || family == DeveloperFamily.GENERAL) {
            return false;
        }

        Set<SkillCategory> categories = skillCategories(project.getSkills());
        double familyScore = categories.stream()
                .map(SkillFamilyWeightMapper::mapWeights)
                .mapToDouble(weights -> weights.getOrDefault(family, 0.0))
                .sum();
        if (familyScore >= 1.0) {
            return true;
        }

        String blob = projectTextBlob(project);
        return familyKeywords(family).stream().anyMatch(blob::contains);
    }

    static String projectTextBlob(PortfolioProject project) {
        List<String> values = new ArrayList<>();
        if (project != null) {
            values.add(normalize(project.getTitle()));
            values.add(normalize(project.getDescription()));
            values.addAll(skillNames(project.getSkills()));
        }
        return String.join(" ", values);
    }

    static List<String> familyKeywords(DeveloperFamily family) {
        return switch (family) {
            case BACKEND -> List.of("backend", "java", "spring", "spring boot", "api", "rest", "sql", "database", "hibernate", "jpa", "server");
            case FRONTEND -> List.of("frontend", "react", "angular", "vue", "typescript", "ui", "ux", "interface", "responsive");
            case FULL_STACK -> List.of("full stack", "fullstack", "frontend", "backend", "api", "react", "spring");
            case DEVOPS_CLOUD -> List.of("devops", "cloud", "docker", "kubernetes", "ci/cd", "deployment", "infrastructure", "monitoring");
            case DATA_AI -> List.of("data", "machine learning", "ai", "analysis", "python", "nlp", "model", "llm");
            case SECURITY -> List.of("security", "cybersecurity", "auth", "iam", "devsecops", "protection", "access control");
            case DESIGN_CREATIVE -> List.of("design", "ui", "ux", "graphic", "motion", "creative", "visual", "brand");
            case GENERAL -> List.of();
        };
    }

    static int countKeywordMatches(Collection<String> names, Collection<String> keywords) {
        if (names == null || keywords == null) {
            return 0;
        }
        int count = 0;
        for (String keyword : keywords) {
            if (names.stream().anyMatch(name -> normalize(name).contains(keyword))) {
                count++;
            }
        }
        return count;
    }

    static int countKeywordMatchesInText(String text, Collection<String> keywords) {
        String normalized = normalize(text);
        int count = 0;
        for (String keyword : keywords) {
            if (normalized.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    static boolean containsAny(Collection<String> names, Collection<String> keywords) {
        return countKeywordMatches(names, keywords) > 0;
    }

    static String formatFamilyLabel(DeveloperFamily family) {
        if (family == null) {
            return "General";
        }
        return switch (family) {
            case FULL_STACK -> "Full Stack";
            case DEVOPS_CLOUD -> "DevOps / Cloud";
            case DATA_AI -> "Data / AI";
            case DESIGN_CREATIVE -> "Design / Creative";
            default -> {
                String lower = family.name().toLowerCase(Locale.ROOT).replace("_", " ");
                yield Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
            }
        };
    }

    static String formatCategoryLabel(SkillCategory category) {
        if (category == null) {
            return "";
        }
        return switch (category) {
            case SQL_DATABASES -> "SQL Databases";
            case NOSQL_DATABASES -> "NoSQL Databases";
            case CI_CD -> "CI/CD";
            case UI_UX_DESIGN -> "UI/UX Design";
            case DEVOPS -> "DevOps";
            case DEVSECOPS -> "DevSecOps";
            case LLM_ENGINEERING -> "LLM Engineering";
            default -> {
                String[] parts = category.name().toLowerCase(Locale.ROOT).split("_");
                StringBuilder result = new StringBuilder();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
                    }
                }
                yield result.toString().trim();
            }
        };
    }

    static List<PortfolioProject> sortedProjects(Portfolio portfolio) {
        if (portfolio == null || portfolio.getProjects() == null) {
            return List.of();
        }
        return portfolio.getProjects().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PortfolioProject::getPinned, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(PortfolioProject::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(PortfolioProject::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    static List<String> topSkillNames(Portfolio portfolio, int limit) {
        if (portfolio == null || portfolio.getSkills() == null) {
            return List.of();
        }
        return portfolio.getSkills().stream()
                .filter(Objects::nonNull)
                .map(Skill::getName)
                .filter(PortfolioAiSupport::hasText)
                .map(String::trim)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .limit(limit)
                .toList();
    }

    static String joinLines(List<String> values) {
        return values == null ? "" : values.stream().filter(PortfolioAiSupport::hasText).collect(Collectors.joining("\n"));
    }

    static Set<String> uniqueOrdered(Collection<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
                .filter(PortfolioAiSupport::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
