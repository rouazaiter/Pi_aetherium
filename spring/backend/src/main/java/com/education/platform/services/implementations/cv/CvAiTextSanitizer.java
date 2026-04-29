package com.education.platform.services.implementations.cv;

import java.util.regex.Pattern;

final class CvAiTextSanitizer {

    private static final Pattern THINK_BLOCK_PATTERN = Pattern.compile("(?is)<think\\b[^>]*>.*?</think>");
    private static final Pattern CODE_FENCE_PATTERN = Pattern.compile("(?is)```(?:[a-z0-9_-]+)?\\s*(.*?)```");
    private static final Pattern BULLET_PATTERN = Pattern.compile("^[\\-*•#]+\\s*");
    private static final Pattern EXCESSIVE_MARKDOWN_PATTERN = Pattern.compile("(?m)^\\s{0,3}(#{1,6}|[-*_]{3,})\\s*");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:professional|improved|rewritten|refined|ats-friendly|cv|resume|project|experience|education|skills|summary|headline|bio)[a-z /_-]{0,60}:\\s*");

    private CvAiTextSanitizer() {
    }

    static String sanitize(String raw) {
        String cleaned = raw == null ? "" : raw;
        cleaned = THINK_BLOCK_PATTERN.matcher(cleaned).replaceAll(" ");
        cleaned = CODE_FENCE_PATTERN.matcher(cleaned).replaceAll("$1");
        cleaned = cleaned.replace('\r', '\n');
        cleaned = EXCESSIVE_MARKDOWN_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> BULLET_PATTERN.matcher(line).replaceFirst(""))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        cleaned = stripPrefixes(cleaned);
        cleaned = stripWrapping(cleaned);
        return cleaned.replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static String stripPrefixes(String value) {
        String cleaned = value == null ? "" : value.trim();
        String previous;
        do {
            previous = cleaned;
            cleaned = PREFIX_PATTERN.matcher(cleaned).replaceFirst("").trim();
        } while (!cleaned.equals(previous));
        return cleaned;
    }

    private static String stripWrapping(String value) {
        String cleaned = value == null ? "" : value.trim();
        boolean changed;
        do {
            changed = false;
            if (cleaned.length() >= 2) {
                char first = cleaned.charAt(0);
                char last = cleaned.charAt(cleaned.length() - 1);
                if ((first == '"' && last == '"')
                        || (first == '\'' && last == '\'')
                        || (first == '`' && last == '`')
                        || (first == '*' && last == '*')) {
                    cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
                    changed = true;
                }
            }
        } while (changed);
        return cleaned;
    }
}
