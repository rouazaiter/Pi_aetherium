package com.education.platform.entities.portfolio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillCategoryConverterTest {

    private final SkillCategoryConverter converter = new SkillCategoryConverter();

    @Test
    void convertToEntityAttributeMapsBlankToOther() {
        assertEquals(SkillCategory.OTHER, converter.convertToEntityAttribute(""));
        assertEquals(SkillCategory.OTHER, converter.convertToEntityAttribute("   "));
        assertEquals(SkillCategory.OTHER, converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttributeMapsLegacyValues() {
        assertEquals(SkillCategory.BACKEND_DEVELOPMENT, converter.convertToEntityAttribute("BACKEND"));
        assertEquals(SkillCategory.FRONTEND_DEVELOPMENT, converter.convertToEntityAttribute("frontend"));
    }

    @Test
    void convertToEntityAttributePreservesCurrentValues() {
        assertEquals(
                SkillCategory.CONTAINERIZATION,
                converter.convertToEntityAttribute("CONTAINERIZATION"));
    }
}
