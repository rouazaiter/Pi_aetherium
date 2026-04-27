package com.education.platform.services.implementations.explore;

import com.education.platform.dto.explore.ExploreOptionDto;
import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.services.implementations.portfolio.ai.SkillFamilyWeightMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class ExploreSearchSupport {

    private static final Map<String, List<String>> JOB_TITLE_KEYWORDS = Map.of(
            "backend", List.of("java", "spring boot", "rest api", "sql", "jpa", "hibernate", "jwt"),
            "frontend", List.of("react", "angular", "typescript", "javascript", "html", "css", "ui"),
            "devops", List.of("docker", "kubernetes", "ci/cd", "github actions", "cloud", "monitoring"),
            "cloud", List.of("docker", "kubernetes", "aws", "cloud", "monitoring", "infrastructure"),
            "data", List.of("python", "machine learning", "ai", "data analysis", "sql"),
            "ai", List.of("python", "machine learning", "ai", "llm", "data analysis", "sql"),
            "security", List.of("security", "jwt", "iam", "access control", "devsecops")
    );

    private ExploreSearchSupport() {
    }

    static List<ExploreOptionDto> familyOptions() {
        return List.of(
                option(DeveloperFamily.BACKEND.name(), "Backend"),
                option(DeveloperFamily.FRONTEND.name(), "Frontend"),
                option(DeveloperFamily.FULL_STACK.name(), "Full Stack"),
                option(DeveloperFamily.DEVOPS_CLOUD.name(), "DevOps / Cloud"),
                option(DeveloperFamily.DATA_AI.name(), "Data / AI"),
                option(DeveloperFamily.SECURITY.name(), "Security"),
                option(DeveloperFamily.DESIGN_CREATIVE.name(), "Design / Creative"),
                option(DeveloperFamily.GENERAL.name(), "General")
        );
    }

    static List<ExploreOptionDto> categoryOptions() {
        return EnumSet.allOf(SkillCategory.class).stream()
                .map(category -> option(category.name(), formatCategoryLabel(category)))
                .sorted((left, right) -> left.getLabel().compareToIgnoreCase(right.getLabel()))
                .toList();
    }

    static ExploreOptionDto option(String value, String label) {
        return ExploreOptionDto.builder()
                .value(value)
                .label(label)
                .build();
    }

    static DeveloperFamily detectFamily(Collection<Skill> skills) {
        Set<SkillCategory> categories = skills == null
                ? Set.of()
                : skills.stream()
                .filter(Objects::nonNull)
                .map(Skill::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SkillCategory.class)));
        return SkillFamilyWeightMapper.dominantFamily(categories);
    }

    static List<String> keywordsForJobTitle(String jobTitle) {
        String normalized = normalize(jobTitle);
        if (normalized.isBlank()) {
            return List.of();
        }

        LinkedHashMap<String, String> keywords = new LinkedHashMap<>();
        keywords.put(normalized, jobTitle.trim());

        for (Map.Entry<String, List<String>> entry : JOB_TITLE_KEYWORDS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                for (String keyword : entry.getValue()) {
                    keywords.putIfAbsent(normalize(keyword), keyword);
                }
            }
        }

        List<String> tokens = new ArrayList<>();
        for (String part : normalized.split("[^a-z0-9+#/.]+")) {
            if (part.length() >= 3) {
                tokens.add(part);
            }
        }
        for (String token : tokens) {
            keywords.putIfAbsent(token, token);
        }

        return new ArrayList<>(keywords.values());
    }

    static boolean matchesText(String blob, String term) {
        return hasText(term) && normalize(blob).contains(normalize(term));
    }

    static boolean matchesPrefixOrContains(String value, String term) {
        if (!hasText(term)) {
            return false;
        }
        String normalizedValue = normalize(value);
        String normalizedTerm = normalize(term);
        return normalizedValue.startsWith(normalizedTerm) || normalizedValue.contains(normalizedTerm);
    }

    static boolean matchesAnyPrefixOrContains(String term, String... values) {
        if (!hasText(term) || values == null || values.length == 0) {
            return false;
        }
        return Arrays.stream(values)
                .anyMatch(value -> matchesPrefixOrContains(value, term));
    }

    static int prefixOrContainsScore(String term, String... values) {
        if (!hasText(term) || values == null || values.length == 0) {
            return 0;
        }
        String normalizedTerm = normalize(term);
        int bestScore = 0;
        for (String value : values) {
            String normalizedValue = normalize(value);
            if (normalizedValue.startsWith(normalizedTerm)) {
                bestScore = Math.max(bestScore, 2);
            } else if (normalizedValue.contains(normalizedTerm)) {
                bestScore = Math.max(bestScore, 1);
            }
        }
        return bestScore;
    }

    static boolean matchesAnyKeyword(String blob, Collection<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }
        String normalizedBlob = normalize(blob);
        return keywords.stream()
                .filter(ExploreSearchSupport::hasText)
                .map(ExploreSearchSupport::normalize)
                .anyMatch(normalizedBlob::contains);
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
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
}
