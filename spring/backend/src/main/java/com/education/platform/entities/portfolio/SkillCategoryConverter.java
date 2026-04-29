package com.education.platform.entities.portfolio;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;
import java.util.Map;

@Converter(autoApply = true)
public class SkillCategoryConverter implements AttributeConverter<SkillCategory, String> {

    private static final Map<String, SkillCategory> LEGACY_VALUES = Map.ofEntries(
            Map.entry("FRONTEND", SkillCategory.FRONTEND_DEVELOPMENT),
            Map.entry("BACKEND", SkillCategory.BACKEND_DEVELOPMENT),
            Map.entry("MOBILE", SkillCategory.MOBILE_DEVELOPMENT),
            Map.entry("FULL_STACK", SkillCategory.FULL_STACK_DEVELOPMENT),
            Map.entry("QA", SkillCategory.QUALITY_ASSURANCE),
            Map.entry("DATABASE", SkillCategory.SQL_DATABASES),
            Map.entry("WRITING", SkillCategory.CONTENT_WRITING),
            Map.entry("MARKETING", SkillCategory.DIGITAL_MARKETING)
    );

    @Override
    public String convertToDatabaseColumn(SkillCategory attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public SkillCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return SkillCategory.OTHER;
        }

        String normalized = dbData.trim().toUpperCase(Locale.ROOT);
        SkillCategory directMatch = LEGACY_VALUES.getOrDefault(normalized, null);
        if (directMatch != null) {
            return directMatch;
        }

        try {
            return SkillCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return SkillCategory.OTHER;
        }
    }
}
