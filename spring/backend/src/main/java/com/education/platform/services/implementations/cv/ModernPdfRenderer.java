package com.education.platform.services.implementations.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.services.interfaces.cv.CVPdfRenderer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ModernPdfRenderer extends AbstractHtmlCvPdfRenderer implements CVPdfRenderer {

    private static final String THEME = "MODERN";

    @Override
    public boolean supports(String theme) {
        return THEME.equalsIgnoreCase(theme);
    }

    @Override
    public String renderHtml(CVPdfRenderData data) {
        CVPdfRenderData.Section profileSection = findSection(data, CVSectionType.PROFILE);
        JsonNode profile = profileSection == null ? null : profileSection.getContent();
        JsonNode settings = data == null ? null : data.getSettings();

        String fullName = firstNonBlank(text(profile, "fullName"), "Curriculum Vitae");
        String headline = firstNonBlank(text(profile, "headline"), "Software Engineer");
        String summary = text(profile, "summary");
        String email = text(profile, "email");
        String phone = text(profile, "phone");
        String location = text(profile, "location");
        String githubUrl = text(profile, "githubUrl");
        String linkedInUrl = firstNonBlank(text(profile, "linkedInUrl"), text(profile, "linkedinUrl"));

        boolean showProfileImage = settings != null && settings.path("showProfileImage").asBoolean(false);
        String imageSource = showProfileImage && data != null ? firstNonBlank(data.getProfileImageSource()) : "";

        CVPdfRenderData.Section skillsSection = findSection(data, CVSectionType.SKILLS);
        CVPdfRenderData.Section experienceSection = findSection(data, CVSectionType.EXPERIENCE);
        CVPdfRenderData.Section projectsSection = findSection(data, CVSectionType.PROJECTS);
        CVPdfRenderData.Section educationSection = findSection(data, CVSectionType.EDUCATION);
        CVPdfRenderData.Section languagesSection = findSection(data, CVSectionType.LANGUAGES);

        StringBuilder body = new StringBuilder();
        body.append("<div class=\"modern-root\"><div class=\"modern-layout\">");
        body.append("<aside class=\"modern-sidebar\">");
        body.append(renderModernAvatar(imageSource, fullName));
        body.append(renderModernContactSection(email, phone, location, githubUrl, linkedInUrl));
        body.append(renderModernSkillsSection(skillsSection));
        body.append(renderModernEducationSection(educationSection, true));
        body.append(renderModernLanguagesSection(languagesSection, true));
        body.append("</aside>");

        body.append("<main class=\"modern-main\">");
        body.append("<header class=\"modern-header\">");
        body.append("<h1 class=\"modern-name\">").append(escape(fullName)).append("</h1>");
        body.append("<div class=\"modern-headline\">").append(escape(headline)).append("</div>");

        List<String> headerContacts = new ArrayList<>();
        addHeaderContact(headerContacts, email);
        addHeaderContact(headerContacts, phone);
        addHeaderContact(headerContacts, location);
        addHeaderContact(headerContacts, githubUrl);
        addHeaderContact(headerContacts, linkedInUrl);
        if (!headerContacts.isEmpty()) {
            body.append("<div class=\"modern-contact-row\">")
                    .append(String.join("", headerContacts))
                    .append("</div>");
        }
        body.append("</header>");

        if (profileSection != null && profileSection.isVisible() && !summary.isBlank()) {
            body.append(renderModernTextSection("Summary", summary));
        }
        body.append(renderModernExperienceSection(experienceSection));
        body.append(renderModernProjectsSection(projectsSection));

        String bottomEducation = renderModernEducationSection(educationSection, false);
        String bottomLanguages = renderModernLanguagesSection(languagesSection, false);
        if (!bottomEducation.isBlank() || !bottomLanguages.isBlank()) {
            body.append("<div class=\"modern-bottom-grid\">");
            body.append(bottomEducation);
            body.append(bottomLanguages);
            body.append("</div>");
        }

        body.append("</main></div></div>");
        return renderModernDocument(body.toString());
    }

    private String renderModernDocument(String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4; margin: 12mm; }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      font-family: Arial, Helvetica, sans-serif;
                      color: #18214f;
                      background: #ffffff;
                    }
                    h1, h2, h3, h4, p { margin: 0; }
                    .modern-root { width: 100%; }
                    .modern-layout {
                      width: 100%;
                      display: table;
                      table-layout: fixed;
                      border: 1px solid #e3def5;
                      border-radius: 18px;
                      overflow: hidden;
                      background: #ffffff;
                    }
                    .modern-sidebar,
                    .modern-main {
                      display: table-cell;
                      vertical-align: top;
                    }
                    .modern-sidebar {
                      width: 30%;
                      padding: 24px 18px 26px;
                      color: #ffffff;
                      background: linear-gradient(180deg, #3324a3 0%, #4332c4 50%, #202c8f 100%);
                    }
                    .modern-main {
                      width: 70%;
                      padding: 28px 28px 30px;
                      background: #ffffff;
                    }
                    .modern-avatar {
                      width: 120px;
                      height: 120px;
                      margin: 0 auto 24px;
                      border-radius: 50%;
                      padding: 5px;
                      background: rgba(255, 255, 255, 0.95);
                    }
                    .modern-avatar img,
                    .modern-avatar__fallback {
                      width: 100%;
                      height: 100%;
                      border-radius: 50%;
                    }
                    .modern-avatar img {
                      display: block;
                      object-fit: cover;
                    }
                    .modern-avatar__fallback {
                      display: table;
                      background: linear-gradient(135deg, #7f66ff 0%, #b37cff 100%);
                      color: #ffffff;
                      font-size: 28px;
                      font-weight: 700;
                      text-align: center;
                    }
                    .modern-avatar__fallback span {
                      display: table-cell;
                      vertical-align: middle;
                    }
                    .modern-sidebar-section {
                      margin-top: 22px;
                      page-break-inside: avoid;
                    }
                    .modern-sidebar-title {
                      font-size: 11pt;
                      font-weight: 700;
                      letter-spacing: 0.04em;
                      text-transform: uppercase;
                    }
                    .modern-sidebar-line {
                      height: 1px;
                      margin: 8px 0 12px;
                      background: rgba(255, 255, 255, 0.5);
                    }
                    .modern-contact-item,
                    .modern-sidebar-entry {
                      margin-bottom: 10px;
                      color: rgba(255, 255, 255, 0.94);
                      font-size: 9.4pt;
                      line-height: 1.45;
                      word-break: break-word;
                    }
                    .modern-skill-list {
                      font-size: 0;
                    }
                    .modern-skill-pill {
                      display: inline-block;
                      margin: 0 6px 6px 0;
                      padding: 5px 10px;
                      border-radius: 8px;
                      font-size: 8.8pt;
                      font-weight: 700;
                      color: #ffffff;
                      background: rgba(255, 255, 255, 0.14);
                      border: 1px solid rgba(255, 255, 255, 0.12);
                    }
                    .modern-sidebar-entry h4 {
                      font-size: 10pt;
                      font-weight: 700;
                      margin-bottom: 3px;
                    }
                    .modern-sidebar-entry p,
                    .modern-sidebar-entry span {
                      display: block;
                      margin-top: 3px;
                    }
                    .modern-language-item { margin-bottom: 12px; }
                    .modern-language-head {
                      width: 100%;
                      margin-bottom: 5px;
                    }
                    .modern-language-name {
                      float: left;
                      font-weight: 700;
                      font-size: 9.4pt;
                    }
                    .modern-language-level {
                      float: right;
                      font-size: 9.2pt;
                    }
                    .modern-language-head::after {
                      content: "";
                      display: block;
                      clear: both;
                    }
                    .modern-language-track {
                      height: 6px;
                      border-radius: 999px;
                      overflow: hidden;
                      background: rgba(255, 255, 255, 0.25);
                    }
                    .modern-language-fill {
                      display: block;
                      height: 100%;
                      border-radius: inherit;
                      background: #ffffff;
                    }
                    .modern-name {
                      font-size: 25pt;
                      line-height: 1.02;
                      font-weight: 700;
                      letter-spacing: -0.04em;
                      text-transform: uppercase;
                      color: #16225a;
                    }
                    .modern-headline {
                      margin-top: 8px;
                      font-size: 14pt;
                      font-weight: 700;
                      color: #6a4ef8;
                      text-transform: uppercase;
                    }
                    .modern-contact-row {
                      margin-top: 14px;
                      font-size: 9.4pt;
                      color: #4b577d;
                    }
                    .modern-contact-chip {
                      display: inline-block;
                      margin: 0 14px 6px 0;
                    }
                    .modern-section {
                      margin-top: 18px;
                      page-break-inside: avoid;
                    }
                    .modern-section-heading {
                      width: 100%;
                      margin-bottom: 10px;
                    }
                    .modern-section-icon {
                      float: left;
                      width: 24px;
                      height: 24px;
                      border-radius: 7px;
                      background: linear-gradient(180deg, #6f49ff 0%, #8a71ff 100%);
                      color: #ffffff;
                      font-size: 9pt;
                      font-weight: 700;
                      text-align: center;
                      line-height: 24px;
                    }
                    .modern-section-title {
                      float: left;
                      margin-left: 10px;
                      color: #6949ff;
                      font-size: 12pt;
                      font-weight: 700;
                      text-transform: uppercase;
                    }
                    .modern-section-rule {
                      display: block;
                      margin-left: 170px;
                      height: 1px;
                      background: #dcd6fb;
                      position: relative;
                      top: 12px;
                    }
                    .modern-section-heading::after {
                      content: "";
                      display: block;
                      clear: both;
                    }
                    .modern-text {
                      font-size: 10.2pt;
                      color: #394467;
                      line-height: 1.6;
                    }
                    .modern-entry {
                      margin-bottom: 16px;
                      padding-bottom: 14px;
                      border-bottom: 1px dashed #dad7eb;
                      page-break-inside: avoid;
                    }
                    .modern-entry:last-child {
                      margin-bottom: 0;
                      padding-bottom: 0;
                      border-bottom: 0;
                    }
                    .modern-entry-head {
                      width: 100%;
                    }
                    .modern-entry-head h3 {
                      float: left;
                      max-width: 70%;
                      font-size: 12pt;
                      font-weight: 700;
                      color: #1d274b;
                    }
                    .modern-entry-date {
                      float: right;
                      color: #6949ff;
                      font-size: 10pt;
                      font-weight: 700;
                    }
                    .modern-entry-head::after {
                      content: "";
                      display: block;
                      clear: both;
                    }
                    .modern-entry-company {
                      margin-top: 4px;
                      font-size: 10pt;
                      font-weight: 700;
                      color: #253153;
                    }
                    .modern-bullets {
                      margin: 8px 0 0 16px;
                      padding: 0;
                      color: #445073;
                      font-size: 10pt;
                      line-height: 1.55;
                    }
                    .modern-bullets li { margin-bottom: 4px; }
                    .modern-project-link {
                      float: right;
                      color: #6a4ef8;
                      text-decoration: none;
                      font-size: 9.2pt;
                      font-weight: 700;
                    }
                    .modern-project-tags {
                      font-size: 0;
                      margin-top: 9px;
                    }
                    .modern-project-tag {
                      display: inline-block;
                      margin: 0 6px 6px 0;
                      padding: 4px 9px;
                      border-radius: 8px;
                      background: #f3efff;
                      border: 1px solid #e4defc;
                      color: #605289;
                      font-size: 8.5pt;
                      font-weight: 700;
                    }
                    .modern-bottom-grid {
                      width: 100%;
                      display: table;
                      table-layout: fixed;
                      margin-top: 6px;
                    }
                    .modern-bottom-grid > .modern-section {
                      display: table-cell;
                      width: 50%;
                      vertical-align: top;
                    }
                    .modern-bottom-grid > .modern-section:first-child {
                      padding-right: 16px;
                    }
                    .modern-bottom-grid > .modern-section:last-child {
                      padding-left: 16px;
                    }
                    .modern-bottom-grid .modern-language-track {
                      background: #dddff0;
                    }
                    .modern-bottom-grid .modern-language-fill {
                      background: linear-gradient(90deg, #704cff 0%, #8f6fff 100%);
                    }
                  </style>
                </head>
                <body>
                __BODY__
                </body>
                </html>
                """.replace("__BODY__", bodyHtml);
    }

    private String renderModernAvatar(String imageSource, String fullName) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"modern-avatar\">");
        if (imageSource != null && !imageSource.isBlank()) {
            html.append("<img src=\"")
                    .append(escape(imageSource))
                    .append("\" alt=\"Profile image\" />");
        } else {
            html.append("<div class=\"modern-avatar__fallback\"><span>")
                    .append(escape(initials(fullName)))
                    .append("</span></div>");
        }
        html.append("</div>");
        return html.toString();
    }

    private String renderModernContactSection(String email, String phone, String location, String githubUrl, String linkedInUrl) {
        List<String> contacts = new ArrayList<>();
        addSidebarContact(contacts, email);
        addSidebarContact(contacts, phone);
        addSidebarContact(contacts, location);
        addSidebarContact(contacts, githubUrl);
        addSidebarContact(contacts, linkedInUrl);
        if (contacts.isEmpty()) {
            return "";
        }
        return renderSidebarSection("Contact", String.join("", contacts));
    }

    private String renderModernSkillsSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder("<div class=\"modern-skill-list\">");
        for (JsonNode group : content) {
            if (group == null || group.isNull()) {
                continue;
            }
            JsonNode skills = group.get("skills");
            if (skills == null || !skills.isArray()) {
                continue;
            }
            for (JsonNode skill : skills) {
                String name = text(skill, "name");
                if (!name.isBlank()) {
                    html.append("<span class=\"modern-skill-pill\">")
                            .append(escape(name))
                            .append("</span>");
                }
            }
        }
        html.append("</div>");
        if (html.toString().equals("<div class=\"modern-skill-list\"></div>")) {
            return "";
        }
        return renderSidebarSection(firstNonBlank(section.getTitle(), "Skills"), html.toString());
    }

    private String renderModernEducationSection(CVPdfRenderData.Section section, boolean sidebar) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder();
        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String school = text(item, "school");
            String degree = text(item, "degree");
            String fieldOfStudy = text(item, "fieldOfStudy");
            String dateRange = formatDateRange(text(item, "startDate"), text(item, "endDate"), item.path("current").asBoolean(false));
            if (school.isBlank() && degree.isBlank() && fieldOfStudy.isBlank() && dateRange.isBlank()) {
                continue;
            }
            if (sidebar) {
                entries.append("<div class=\"modern-sidebar-entry\">")
                        .append("<h4>").append(escape(firstNonBlank(school, degree, "Education"))).append("</h4>");
                if (!degree.isBlank() || !fieldOfStudy.isBlank()) {
                    entries.append("<p>").append(escape(joinNonBlank(" - ", degree, fieldOfStudy))).append("</p>");
                }
                if (!dateRange.isBlank()) {
                    entries.append("<span>").append(escape(dateRange)).append("</span>");
                }
                entries.append("</div>");
            } else {
                entries.append("<article class=\"modern-entry\">")
                        .append("<div class=\"modern-entry-head\"><h3>").append(escape(firstNonBlank(school, "School"))).append("</h3>");
                if (!dateRange.isBlank()) {
                    entries.append("<span class=\"modern-entry-date\">").append(escape(dateRange)).append("</span>");
                }
                entries.append("</div>");
                if (!degree.isBlank() || !fieldOfStudy.isBlank()) {
                    entries.append("<div class=\"modern-entry-company\">")
                            .append(escape(joinNonBlank(" - ", degree, fieldOfStudy)))
                            .append("</div>");
                }
                entries.append("</article>");
            }
        }
        if (entries.isEmpty()) {
            return "";
        }
        if (sidebar) {
            return renderSidebarSection(firstNonBlank(section.getTitle(), "Education"), entries.toString());
        }
        return renderMainSection(firstNonBlank(section.getTitle(), "Education"), "ED", entries.toString());
    }

    private String renderModernLanguagesSection(CVPdfRenderData.Section section, boolean sidebar) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder();
        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String name = text(item, "name");
            String level = text(item, "proficiency");
            if (name.isBlank() && level.isBlank()) {
                continue;
            }
            entries.append("<div class=\"modern-language-item\">")
                    .append("<div class=\"modern-language-head\">")
                    .append("<span class=\"modern-language-name\">").append(escape(firstNonBlank(name, "Language"))).append("</span>")
                    .append("<span class=\"modern-language-level\">").append(escape(level.isBlank() ? "Level" : level)).append("</span>")
                    .append("</div>")
                    .append("<div class=\"modern-language-track\"><span class=\"modern-language-fill\" style=\"width:")
                    .append(languagePercent(level))
                    .append("%;\"></span></div></div>");
        }
        if (entries.isEmpty()) {
            return "";
        }
        if (sidebar) {
            return renderSidebarSection(firstNonBlank(section.getTitle(), "Languages"), entries.toString());
        }
        return renderMainSection(firstNonBlank(section.getTitle(), "Languages"), "LA", entries.toString());
    }

    private String renderModernTextSection(String title, String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return renderMainSection(title, "S", "<p class=\"modern-text\">" + escape(text) + "</p>");
    }

    private String renderModernExperienceSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder();
        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String role = text(item, "role");
            String company = text(item, "company");
            String summary = text(item, "summary");
            String dateRange = formatDateRange(text(item, "startDate"), text(item, "endDate"), item.path("current").asBoolean(false));
            if (role.isBlank() && company.isBlank() && summary.isBlank()) {
                continue;
            }
            entries.append("<article class=\"modern-entry\">")
                    .append("<div class=\"modern-entry-head\"><h3>")
                    .append(escape(firstNonBlank(role, "Role")))
                    .append("</h3>");
            if (!dateRange.isBlank()) {
                entries.append("<span class=\"modern-entry-date\">").append(escape(dateRange)).append("</span>");
            }
            entries.append("</div>");
            if (!company.isBlank()) {
                entries.append("<div class=\"modern-entry-company\">").append(escape(company)).append("</div>");
            }
            if (!summary.isBlank()) {
                entries.append(renderBulletList(summary));
            }
            entries.append("</article>");
        }
        if (entries.isEmpty()) {
            return "";
        }
        return renderMainSection(firstNonBlank(section.getTitle(), "Experience"), "EX", entries.toString());
    }

    private String renderModernProjectsSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder();
        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String title = text(item, "title");
            String description = text(item, "description");
            String projectUrl = text(item, "projectUrl");
            List<String> skillNames = readSkillNames(item.get("skills"));
            if (title.isBlank() && description.isBlank() && projectUrl.isBlank() && skillNames.isEmpty()) {
                continue;
            }
            entries.append("<article class=\"modern-entry\">")
                    .append("<div class=\"modern-entry-head\"><h3>")
                    .append(escape(firstNonBlank(title, "Untitled Project")))
                    .append("</h3>");
            if (!projectUrl.isBlank()) {
                entries.append("<a class=\"modern-project-link\" href=\"")
                        .append(escape(projectUrl))
                        .append("\">Open</a>");
            }
            entries.append("</div>");
            if (!description.isBlank()) {
                entries.append("<p class=\"modern-text\">").append(escape(description)).append("</p>");
            } else {
                entries.append("<p class=\"modern-text\">Project description missing</p>");
            }
            if (!skillNames.isEmpty()) {
                entries.append("<div class=\"modern-project-tags\">");
                for (String skillName : skillNames) {
                    entries.append("<span class=\"modern-project-tag\">").append(escape(skillName)).append("</span>");
                }
                entries.append("</div>");
            }
            entries.append("</article>");
        }
        if (entries.isEmpty()) {
            return "";
        }
        return renderMainSection(firstNonBlank(section.getTitle(), "Projects"), "PR", entries.toString());
    }

    private String renderSidebarSection(String title, String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return "";
        }
        return "<section class=\"modern-sidebar-section\"><div class=\"modern-sidebar-title\">"
                + escape(title)
                + "</div><div class=\"modern-sidebar-line\"></div>"
                + contentHtml
                + "</section>";
    }

    private String renderMainSection(String title, String iconText, String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return "";
        }
        return "<section class=\"modern-section\"><div class=\"modern-section-heading\">"
                + "<span class=\"modern-section-icon\">" + escape(iconText) + "</span>"
                + "<span class=\"modern-section-title\">" + escape(title) + "</span>"
                + "<span class=\"modern-section-rule\"></span></div>"
                + contentHtml
                + "</section>";
    }

    private String renderBulletList(String summary) {
        String[] lines = summary.split("\\r?\\n");
        StringBuilder bullets = new StringBuilder("<ul class=\"modern-bullets\">");
        boolean hasItems = false;
        for (String line : lines) {
            String trimmed = line == null ? "" : line.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            hasItems = true;
            bullets.append("<li>")
                    .append(escape(trimmed.replaceFirst("^[\\u2022\\-*]+\\s*", "")))
                    .append("</li>");
        }
        bullets.append("</ul>");
        if (!hasItems) {
            return "<p class=\"modern-text\">" + escape(summary) + "</p>";
        }
        return bullets.toString();
    }

    private void addSidebarContact(List<String> contacts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        contacts.add("<div class=\"modern-contact-item\">" + escape(value) + "</div>");
    }

    private void addHeaderContact(List<String> contacts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        contacts.add("<span class=\"modern-contact-chip\">" + escape(value) + "</span>");
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

    private String initials(String fullName) {
        String[] parts = fullName == null ? new String[0] : fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.isEmpty() ? "CV" : initials.toString();
    }

    private int languagePercent(String proficiency) {
        String normalized = proficiency == null ? "" : proficiency.trim().toUpperCase();
        return switch (normalized) {
            case "A1" -> 20;
            case "A2" -> 32;
            case "B1" -> 50;
            case "B2" -> 68;
            case "C1" -> 84;
            case "C2" -> 96;
            default -> 58;
        };
    }
}
