package com.education.platform.services.implementations.portfolio.ai;

import java.util.regex.Pattern;

final class PortfolioAiTextSanitizer {

    private static final Pattern THINK_BLOCK_PATTERN = Pattern.compile("(?is)<think\\b[^>]*>.*?</think>");
    private static final Pattern CODE_FENCE_PATTERN = Pattern.compile("(?is)```(?:[a-z0-9_-]+)?\\s*(.*?)```");
    private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^\\s{0,3}(#{1,6}|[-*_]{3,})\\s*");

    private PortfolioAiTextSanitizer() {
    }

    static String sanitize(String raw) {
        String cleaned = raw == null ? "" : raw;
        cleaned = THINK_BLOCK_PATTERN.matcher(cleaned).replaceAll(" ");
        cleaned = CODE_FENCE_PATTERN.matcher(cleaned).replaceAll("$1");
        cleaned = cleaned.replace('\r', '\n');
        cleaned = HEADING_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        return cleaned.replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }
}
