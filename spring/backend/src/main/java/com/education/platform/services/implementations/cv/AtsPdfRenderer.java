package com.education.platform.services.implementations.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.services.interfaces.cv.CVPdfRenderer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AtsPdfRenderer extends AbstractHtmlCvPdfRenderer implements CVPdfRenderer {

    private static final String THEME = "ATS_MINIMAL";

    @Override
    public boolean supports(String theme) {
        return THEME.equalsIgnoreCase(theme);
    }

    @Override
    public String renderHtml(CVPdfRenderData data) {
        CVPdfRenderData.Section profileSection = findSection(data, CVSectionType.PROFILE);
        JsonNode profile = profileSection == null ? null : profileSection.getContent();
        JsonNode settings = data == null ? null : data.getSettings();

        String fullName = text(profile, "fullName");
        String headline = text(profile, "headline");
        String summary = text(profile, "summary");
        String email = text(profile, "email");
        String phone = text(profile, "phone");
        String location = text(profile, "location");
        String githubUrl = text(profile, "githubUrl");
        String linkedInUrl = firstNonBlank(text(profile, "linkedInUrl"), text(profile, "linkedinUrl"));
        String ignoredProfilePicture = text(profile, "profilePicture");
        boolean ignoredShowProfileImage = settings != null && settings.path("showProfileImage").asBoolean(false);
        if (!ignoredProfilePicture.isBlank() || ignoredShowProfileImage) {
            // ATS output stays text-only even when image data or image settings exist on the draft.
        }

        StringBuilder body = new StringBuilder();
        body.append("<div class=\"cv-root\">");
        body.append("<header class=\"cv-header\">");
        body.append("<div class=\"cv-name\">").append(escape(firstNonBlank(fullName, "Curriculum Vitae"))).append("</div>");
        if (!headline.isBlank()) {
            body.append("<div class=\"cv-headline\">").append(escape(headline)).append("</div>");
        }

        List<String> contactParts = new ArrayList<>();
        addContactPart(contactParts, email);
        addContactPart(contactParts, phone);
        addContactPart(contactParts, location);
        addContactPart(contactParts, githubUrl);
        addContactPart(contactParts, linkedInUrl);
        if (!contactParts.isEmpty()) {
            body.append("<div class=\"cv-contact\">")
                    .append(String.join("", contactParts))
                    .append("</div>");
        }
        body.append("</header>");

        if (!summary.isBlank()) {
            body.append("<section class=\"cv-section\">")
                    .append("<div class=\"cv-section-title\">Professional Summary</div>")
                    .append("<p class=\"cv-item-text\">")
                    .append(escape(summary))
                    .append("</p>")
                    .append("</section>");
        }

        for (CVPdfRenderData.Section section : visibleSections(data)) {
            if (section.getType() == CVSectionType.PROFILE) {
                continue;
            }
            body.append(renderSection(section));
        }

        body.append("</div>");
        return renderDocument("CV ATS", "theme-ats-minimal", body.toString());
    }

    private void addContactPart(List<String> contactParts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        contactParts.add("<span>" + escape(value) + "</span>");
    }

    private String renderSection(CVPdfRenderData.Section section) {
        return switch (section.getType()) {
            case SKILLS -> renderSkillsSection(section);
            case PROJECTS -> renderProjectsSection(section);
            case EXPERIENCE -> renderExperienceSection(section);
            case EDUCATION -> renderEducationSection(section);
            case LANGUAGES -> renderLanguagesSection(section);
            default -> "";
        };
    }

    private String renderSkillsSection(CVPdfRenderData.Section section) {
        JsonNode content = section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<section class=\"cv-section\"><div class=\"cv-section-title\">")
                .append(escape(firstNonBlank(section.getTitle(), "Skills")))
                .append("</div>");

        for (JsonNode group : content) {
            if (group == null || group.isNull()) {
                continue;
            }
            String category = text(group, "category");
            JsonNode skills = group.get("skills");
            List<String> names = new ArrayList<>();
            if (skills != null && skills.isArray()) {
                for (JsonNode skill : skills) {
                    String name = text(skill, "name");
                    if (!name.isBlank()) {
                        names.add(name);
                    }
                }
            }
            if (names.isEmpty()) {
                continue;
            }
            html.append("<div class=\"cv-skill-group\">");
            if (!category.isBlank()) {
                html.append("<span class=\"cv-skill-category\">")
                        .append(escape(category))
                        .append(":</span> ");
            }
            html.append("<span>")
                    .append(escape(String.join(", ", names)))
                    .append("</span></div>");
        }

        html.append("</section>");
        return html.toString();
    }

    private String renderProjectsSection(CVPdfRenderData.Section section) {
        return renderItemArraySection(section, "Projects", item -> {
            String title = text(item, "title");
            String description = text(item, "description");
            String projectUrl = text(item, "projectUrl");
            String collectionName = text(item, "collectionName");
            List<String> skillNames = readSkillNames(item.get("skills"));
            String meta = joinNonBlank(" | ",
                    collectionName,
                    skillNames.isEmpty() ? null : String.join(", ", skillNames),
                    projectUrl);
            return renderItem(title, meta, null, description);
        });
    }

    private String renderExperienceSection(CVPdfRenderData.Section section) {
        return renderItemArraySection(section, "Experience", item -> {
            String title = joinNonBlank(" - ", text(item, "role"), text(item, "company"));
            String subtitle = text(item, "location");
            String meta = joinNonBlank(" - ",
                    formatDateRange(text(item, "startDate"), text(item, "endDate"), item.path("current").asBoolean(false)),
                    subtitle);
            return renderItem(title, meta, null, text(item, "summary"));
        });
    }

    private String renderEducationSection(CVPdfRenderData.Section section) {
        return renderItemArraySection(section, "Education", item -> {
            String title = joinNonBlank(" - ", text(item, "degree"), text(item, "school"));
            String subtitle = joinNonBlank(" - ", text(item, "fieldOfStudy"), text(item, "location"));
            String meta = formatDateRange(text(item, "startDate"), text(item, "endDate"), item.path("current").asBoolean(false));
            return renderItem(title, meta, subtitle, text(item, "description"));
        });
    }

    private String renderLanguagesSection(CVPdfRenderData.Section section) {
        return renderItemArraySection(section, "Languages", item -> {
            String title = text(item, "name");
            String subtitle = text(item, "proficiency");
            return renderItem(title, subtitle, null, null);
        });
    }

    private String renderItemArraySection(CVPdfRenderData.Section section, String fallbackTitle, SectionItemRenderer renderer) {
        JsonNode content = section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<section class=\"cv-section\"><div class=\"cv-section-title\">")
                .append(escape(firstNonBlank(section.getTitle(), fallbackTitle)))
                .append("</div>");

        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String itemHtml = renderer.render(item);
            if (!itemHtml.isBlank()) {
                html.append(itemHtml);
            }
        }

        html.append("</section>");
        return html.toString();
    }

    private String renderItem(String title, String meta, String subtitle, String text) {
        if (title == null || title.isBlank()) {
            title = firstNonBlank(subtitle, meta);
        }
        if (title == null || title.isBlank()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"cv-item\">");
        html.append("<div class=\"cv-item-title\">").append(escape(title)).append("</div>");
        if (subtitle != null && !subtitle.isBlank()) {
            html.append("<div class=\"cv-item-subtitle\">").append(escape(subtitle)).append("</div>");
        }
        if (meta != null && !meta.isBlank()) {
            html.append("<div class=\"cv-item-meta\">").append(escape(meta)).append("</div>");
        }
        if (text != null && !text.isBlank()) {
            html.append("<div class=\"cv-item-text\">").append(escape(text)).append("</div>");
        }
        html.append("</div>");
        return html.toString();
    }

    private List<String> readSkillNames(JsonNode skills) {
        if (skills == null || !skills.isArray()) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (JsonNode skill : skills) {
            String name = text(skill, "name");
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    private String formatDateRange(String startDate, String endDate, boolean current) {
        String end = current ? "Present" : endDate;
        return joinNonBlank(" - ", startDate, end);
    }

    private String firstNonBlank(String... values) {
        return java.util.Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse("");
    }

    @FunctionalInterface
    private interface SectionItemRenderer {
        String render(JsonNode item);
    }
}
