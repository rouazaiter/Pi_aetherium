package com.education.platform.services.interfaces.cv;

import java.util.Set;

public final class CVTemplateConstants {

    public static final String DEVELOPER_MINIMAL = "developer-minimal";
    public static final String MODERN = "modern";
    public static final String ACADEMIC = "academic";
    public static final String DEFAULT_TEMPLATE = DEVELOPER_MINIMAL;
    public static final Set<String> SUPPORTED_TEMPLATES = Set.of(
            DEVELOPER_MINIMAL,
            MODERN,
            ACADEMIC
    );

    private CVTemplateConstants() {
    }

    public static String normalizeTemplate(String template) {
        if (template == null || template.isBlank()) {
            return DEFAULT_TEMPLATE;
        }

        String normalized = template.trim();
        return SUPPORTED_TEMPLATES.contains(normalized) ? normalized : DEFAULT_TEMPLATE;
    }
}
