package com.education.platform.services.implementations.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

abstract class AbstractHtmlCvPdfRenderer {

    protected String renderDocument(String title, String bodyClassName, String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4; margin: 18mm; }
                    * { box-sizing: border-box; }
                    body {
                      font-family: Arial, Helvetica, sans-serif;
                      color: #111827;
                      font-size: 11pt;
                      line-height: 1.45;
                      margin: 0;
                    }
                    h1, h2, h3, p { margin: 0; }
                    .cv-root { width: 100%%; }
                    .cv-header { margin-bottom: 18px; }
                    .cv-name {
                      font-size: 22pt;
                      font-weight: 700;
                      margin-bottom: 4px;
                    }
                    .cv-headline {
                      font-size: 12pt;
                      color: #374151;
                      margin-bottom: 8px;
                    }
                    .cv-contact {
                      font-size: 10pt;
                      color: #4b5563;
                    }
                    .cv-contact span { margin-right: 12px; }
                    .cv-section {
                      margin-top: 16px;
                      page-break-inside: avoid;
                    }
                    .cv-section-title {
                      font-size: 11pt;
                      font-weight: 700;
                      text-transform: uppercase;
                      letter-spacing: 0.04em;
                      padding-bottom: 5px;
                      border-bottom: 1px solid #d1d5db;
                      margin-bottom: 8px;
                    }
                    .cv-item { margin-bottom: 10px; }
                    .cv-item:last-child { margin-bottom: 0; }
                    .cv-item-title {
                      font-size: 11pt;
                      font-weight: 700;
                    }
                    .cv-item-subtitle {
                      color: #374151;
                      margin-top: 2px;
                    }
                    .cv-item-meta {
                      color: #6b7280;
                      font-size: 9.5pt;
                      margin-top: 2px;
                    }
                    .cv-item-text {
                      margin-top: 4px;
                      white-space: pre-line;
                    }
                    .cv-skill-group { margin-bottom: 8px; }
                    .cv-skill-category { font-weight: 700; }
                    .cv-muted { color: #6b7280; }
                    .cv-link {
                      color: #111827;
                      text-decoration: none;
                    }
                    .cv-placeholder-note {
                      margin-top: 10px;
                      padding: 10px 12px;
                      border: 1px solid #d1d5db;
                      background: #f9fafb;
                    }
                  </style>
                  <title>%s</title>
                </head>
                <body class="%s">
                  %s
                </body>
                </html>
                """.formatted(escape(title), escape(bodyClassName), bodyHtml);
    }

    protected List<CVPdfRenderData.Section> visibleSections(CVPdfRenderData data) {
        if (data == null || data.getSections() == null) {
            return List.of();
        }
        return data.getSections().stream()
                .filter(Objects::nonNull)
                .filter(CVPdfRenderData.Section::isVisible)
                .sorted(Comparator
                        .comparing((CVPdfRenderData.Section section) -> section.getOrderIndex() == null ? Integer.MAX_VALUE : section.getOrderIndex())
                        .thenComparing(section -> section.getType() == null ? "" : section.getType().name()))
                .toList();
    }

    protected CVPdfRenderData.Section findSection(CVPdfRenderData data, CVSectionType type) {
        return visibleSections(data).stream()
                .filter(section -> section.getType() == type)
                .findFirst()
                .orElse(null);
    }

    protected String renderTextSection(String title, JsonNode content, String... fields) {
        if (content == null || content.isNull()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (String field : fields) {
            String value = text(content, field);
            if (!value.isBlank()) {
                parts.add("<p class=\"cv-item-text\">" + escape(value) + "</p>");
            }
        }
        if (parts.isEmpty()) {
            return "";
        }
        return "<section class=\"cv-section\"><div class=\"cv-section-title\">"
                + escape(title)
                + "</div>"
                + String.join("", parts)
                + "</section>";
    }

    protected String text(JsonNode node, String fieldName) {
        if (node == null || node.isNull() || fieldName == null) {
            return "";
        }
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.asText("").trim();
    }

    protected String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    protected String joinNonBlank(String separator, String... values) {
        return java.util.Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(separator));
    }
}
