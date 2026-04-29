package com.education.platform.services.interfaces.cv;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CVTemplateConstants {

    public static final String ATS_MINIMAL = "ATS_MINIMAL";
    public static final String MODERN = "MODERN";
    public static final String ELEGANT = "ELEGANT";
    public static final String CREATIVE = "CREATIVE";
    public static final String DEFAULT_TEMPLATE = ATS_MINIMAL;
    public static final Set<String> SUPPORTED_TEMPLATES = Set.of(
            ATS_MINIMAL,
            MODERN,
            ELEGANT,
            CREATIVE
    );
    private static final Map<String, String> LEGACY_TEMPLATE_ALIASES = Map.of(
            "DEVELOPER-MINIMAL", ATS_MINIMAL,
            "MODERN", MODERN,
            "ACADEMIC", ELEGANT
    );

    private CVTemplateConstants() {
    }

    public static String normalizeTemplate(String template) {
        if (template == null || template.isBlank()) {
            return DEFAULT_TEMPLATE;
        }

        String normalized = template.trim().toUpperCase(Locale.ROOT);
        return SUPPORTED_TEMPLATES.contains(normalized) ? normalized : DEFAULT_TEMPLATE;
    }

    public static String normalizeTemplateOrAlias(String template) {
        if (template == null || template.isBlank()) {
            return DEFAULT_TEMPLATE;
        }

        String normalized = template.trim().toUpperCase(Locale.ROOT);
        if (SUPPORTED_TEMPLATES.contains(normalized)) {
            return normalized;
        }
        return LEGACY_TEMPLATE_ALIASES.getOrDefault(normalized, DEFAULT_TEMPLATE);
    }
}
