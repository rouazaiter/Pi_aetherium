package com.education.platform.services.implementations.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.services.interfaces.cv.CVPdfRenderer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ElegantPdfRenderer extends AbstractHtmlCvPdfRenderer implements CVPdfRenderer {

    private static final String THEME = "ELEGANT";

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
        body.append("<div class=\"elegant-root\"><div class=\"elegant-layout\">");
        body.append("<aside class=\"elegant-sidebar\">");
        body.append(renderElegantAvatar(imageSource, fullName));
        body.append("<div class=\"elegant-ornament\"></div>");
        body.append(renderElegantContactSection(email, phone, location, githubUrl, linkedInUrl));
        body.append(renderElegantSkillsSection(skillsSection));
        body.append(renderElegantEducationSection(educationSection, true));
        body.append(renderElegantLanguagesSection(languagesSection, true));
        body.append("</aside>");

        body.append("<main class=\"elegant-main\">");
        body.append("<header class=\"elegant-header\">");
        body.append("<h1 class=\"elegant-name\">").append(escape(fullName)).append("</h1>");
        body.append("<div class=\"elegant-crest\"><span></span><i></i><span></span></div>");
        body.append("<div class=\"elegant-headline\">").append(escape(headline)).append("</div>");

        List<String> headerContacts = new ArrayList<>();
        addHeaderContact(headerContacts, email);
        addHeaderContact(headerContacts, phone);
        addHeaderContact(headerContacts, location);
        addHeaderContact(headerContacts, githubUrl);
        addHeaderContact(headerContacts, linkedInUrl);
        if (!headerContacts.isEmpty()) {
            body.append("<div class=\"elegant-contact-row\">")
                    .append(String.join("", headerContacts))
                    .append("</div>");
        }
        body.append("</header>");

        if (profileSection != null && profileSection.isVisible() && !summary.isBlank()) {
            body.append(renderElegantTextSection("Profile", "P", summary));
        }
        body.append(renderElegantExperienceSection(experienceSection));
        body.append(renderElegantProjectsSection(projectsSection));

        String bottomEducation = renderElegantEducationSection(educationSection, false);
        String bottomLanguages = renderElegantLanguagesSection(languagesSection, false);
        if (!bottomEducation.isBlank() || !bottomLanguages.isBlank()) {
            body.append("<div class=\"elegant-bottom-grid\">");
            body.append(bottomEducation);
            body.append(bottomLanguages);
            body.append("</div>");
        }

        body.append("</main></div></div>");
        return renderElegantDocument(body.toString());
    }

    private String renderElegantDocument(String bodyHtml) {
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
                      color: #2f3352;
                      background: #fffdf9;
                    }
                    h1, h2, h3, h4, p { margin: 0; }
                    .elegant-root { width: 100%; }
                    .elegant-layout {
                      width: 100%;
                      display: table;
                      table-layout: fixed;
                      border: 1px solid #d9c9a5;
                      border-radius: 18px;
                      overflow: hidden;
                      background: #fffdf9;
                    }
                    .elegant-sidebar,
                    .elegant-main {
                      display: table-cell;
                      vertical-align: top;
                    }
                    .elegant-sidebar {
                      width: 34%;
                      padding: 24px 18px 26px;
                      color: #313566;
                      background:
                        radial-gradient(circle at top left, rgba(231, 223, 247, 0.7), transparent 36%),
                        linear-gradient(180deg, #faf8ff 0%, #fcfaf6 100%);
                      border-right: 1px solid rgba(217, 191, 142, 0.28);
                    }
                    .elegant-main {
                      width: 66%;
                      padding: 28px 28px 30px;
                      background:
                        radial-gradient(circle at top right, rgba(255, 251, 241, 0.75), transparent 34%),
                        linear-gradient(180deg, #fffefb 0%, #fffdf8 100%);
                    }
                    .elegant-avatar {
                      width: 128px;
                      height: 128px;
                      margin: 0 auto 16px;
                      border-radius: 50%;
                      padding: 4px;
                      background: linear-gradient(180deg, rgba(154, 138, 210, 0.78) 0%, rgba(207, 176, 122, 0.86) 100%);
                    }
                    .elegant-avatar img,
                    .elegant-avatar__fallback {
                      width: 100%;
                      height: 100%;
                      border-radius: 50%;
                    }
                    .elegant-avatar img {
                      display: block;
                      object-fit: cover;
                      border: 4px solid rgba(255, 255, 255, 0.96);
                    }
                    .elegant-avatar__fallback {
                      display: table;
                      border: 4px solid rgba(255, 255, 255, 0.96);
                      background: linear-gradient(135deg, #8a74c7 0%, #cfb07a 100%);
                      color: #ffffff;
                      font-family: Georgia, "Times New Roman", serif;
                      font-size: 30px;
                      font-weight: 700;
                      text-align: center;
                    }
                    .elegant-avatar__fallback span {
                      display: table-cell;
                      vertical-align: middle;
                    }
                    .elegant-ornament {
                      width: 110px;
                      height: 16px;
                      margin: 0 auto 14px;
                      position: relative;
                    }
                    .elegant-ornament::before {
                      content: "";
                      position: absolute;
                      left: 0;
                      right: 0;
                      top: 50%;
                      height: 1px;
                      background: linear-gradient(90deg, transparent 0%, rgba(203, 165, 96, 0.9) 50%, transparent 100%);
                    }
                    .elegant-ornament::after {
                      content: "";
                      position: absolute;
                      inset: 0;
                      margin: auto;
                      width: 10px;
                      height: 10px;
                      border: 1px solid rgba(203, 165, 96, 0.95);
                      background: #fffdf8;
                      transform: rotate(45deg);
                    }
                    .elegant-sidebar-section {
                      margin-top: 22px;
                      page-break-inside: avoid;
                    }
                    .elegant-sidebar-heading,
                    .elegant-crest {
                      width: 100%;
                    }
                    .elegant-sidebar-heading::after,
                    .elegant-crest::after,
                    .elegant-section-heading::after,
                    .elegant-entry-head::after,
                    .elegant-language-head::after {
                      content: "";
                      display: block;
                      clear: both;
                    }
                    .elegant-sidebar-rule,
                    .elegant-crest-rule {
                      display: block;
                      height: 1px;
                      background: linear-gradient(90deg, rgba(214, 187, 136, 0.85), rgba(152, 135, 208, 0.45));
                    }
                    .elegant-sidebar-heading h3,
                    .elegant-section-title,
                    .elegant-name {
                      font-family: Georgia, "Times New Roman", serif;
                    }
                    .elegant-sidebar-heading h3 {
                      margin: 0;
                      text-align: center;
                      color: #35336b;
                      font-size: 11pt;
                      font-weight: 500;
                      letter-spacing: 0.1em;
                      text-transform: uppercase;
                    }
                    .elegant-sidebar-heading .elegant-sidebar-rule:first-child,
                    .elegant-sidebar-heading .elegant-sidebar-rule:last-child {
                      width: 32%;
                      margin-top: 10px;
                    }
                    .elegant-sidebar-heading .elegant-sidebar-rule:first-child { float: left; }
                    .elegant-sidebar-heading .elegant-sidebar-rule:last-child { float: right; }
                    .elegant-sidebar-stack {
                      margin-top: 12px;
                    }
                    .elegant-sidebar-stack p,
                    .elegant-sidebar-entry p,
                    .elegant-sidebar-entry span {
                      color: #50557b;
                      font-size: 9.4pt;
                      line-height: 1.55;
                    }
                    .elegant-sidebar-stack p + p,
                    .elegant-sidebar-entry + .elegant-sidebar-entry {
                      margin-top: 10px;
                    }
                    .elegant-skill-list {
                      font-size: 0;
                      margin-top: 12px;
                    }
                    .elegant-skill-pill,
                    .elegant-project-pill {
                      display: inline-block;
                      margin: 0 6px 6px 0;
                      padding: 5px 11px;
                      border-radius: 999px;
                      border: 1px solid rgba(157, 138, 212, 0.72);
                      background: rgba(255, 255, 255, 0.84);
                      color: #4d4b83;
                      font-size: 8.8pt;
                      font-weight: 500;
                    }
                    .elegant-sidebar-entry h4,
                    .elegant-entry h3 {
                      color: #222752;
                      font-size: 11pt;
                      font-weight: 500;
                      font-family: Georgia, "Times New Roman", serif;
                    }
                    .elegant-sidebar-entry span {
                      display: block;
                      margin-top: 3px;
                    }
                    .elegant-language-item { margin-top: 12px; }
                    .elegant-language-head { width: 100%; margin-bottom: 5px; }
                    .elegant-language-name {
                      float: left;
                      color: #303363;
                      font-size: 9.4pt;
                      font-weight: 500;
                      font-family: Georgia, "Times New Roman", serif;
                    }
                    .elegant-language-level {
                      float: right;
                      color: #6a6885;
                      font-size: 8.8pt;
                    }
                    .elegant-language-track {
                      height: 5px;
                      border-radius: 999px;
                      overflow: hidden;
                      background: rgba(186, 176, 214, 0.32);
                    }
                    .elegant-language-fill {
                      display: block;
                      height: 100%;
                      border-radius: inherit;
                      background: linear-gradient(90deg, #9678d2 0%, #d8b26e 100%);
                    }
                    .elegant-name {
                      font-size: 34pt;
                      line-height: 0.94;
                      font-weight: 500;
                      letter-spacing: -0.05em;
                      text-transform: uppercase;
                      color: #202858;
                    }
                    .elegant-crest {
                      margin: 12px 0 14px;
                    }
                    .elegant-crest .elegant-crest-rule:first-child,
                    .elegant-crest .elegant-crest-rule:last-child {
                      width: 38%;
                      float: left;
                      margin-top: 7px;
                    }
                    .elegant-crest .elegant-crest-rule:last-child { float: right; }
                    .elegant-crest i {
                      display: block;
                      margin: 0 auto;
                      width: 9px;
                      height: 9px;
                      border: 1px solid rgba(203, 165, 96, 0.95);
                      background: #fffdf8;
                      transform: rotate(45deg);
                    }
                    .elegant-headline {
                      color: #6654a6;
                      font-size: 14pt;
                      font-weight: 500;
                      letter-spacing: 0.2em;
                      text-transform: uppercase;
                    }
                    .elegant-contact-row {
                      margin-top: 12px;
                      color: #605f77;
                      font-size: 9.2pt;
                    }
                    .elegant-contact-chip {
                      display: inline-block;
                      margin: 0 12px 6px 0;
                    }
                    .elegant-section {
                      margin-top: 18px;
                      page-break-inside: avoid;
                    }
                    .elegant-section-heading {
                      width: 100%;
                      margin-bottom: 10px;
                    }
                    .elegant-section-badge {
                      float: left;
                      width: 26px;
                      height: 26px;
                      border-radius: 50%;
                      border: 1px solid rgba(151, 132, 204, 0.8);
                      color: #6c5daa;
                      background: rgba(255, 255, 255, 0.88);
                      text-align: center;
                      line-height: 24px;
                      font-size: 9pt;
                      font-weight: 700;
                      font-family: Georgia, "Times New Roman", serif;
                    }
                    .elegant-section-title {
                      float: left;
                      margin-left: 12px;
                      color: #283064;
                      font-size: 12pt;
                      font-weight: 500;
                      text-transform: uppercase;
                      letter-spacing: 0.04em;
                    }
                    .elegant-section-line {
                      display: block;
                      margin-left: 180px;
                      height: 1px;
                      background: linear-gradient(90deg, rgba(151, 132, 204, 0.45), rgba(209, 180, 122, 0.8));
                      position: relative;
                      top: 12px;
                    }
                    .elegant-text {
                      color: #404669;
                      font-size: 10pt;
                      line-height: 1.65;
                    }
                    .elegant-entry {
                      margin-bottom: 16px;
                      padding-bottom: 14px;
                      border-bottom: 1px solid rgba(221, 211, 190, 0.72);
                    }
                    .elegant-entry:last-child {
                      margin-bottom: 0;
                      padding-bottom: 0;
                      border-bottom: 0;
                    }
                    .elegant-entry-head { width: 100%; }
                    .elegant-entry-head h3 {
                      float: left;
                      max-width: 68%;
                    }
                    .elegant-entry-date {
                      float: right;
                      color: #72698e;
                      font-size: 9pt;
                    }
                    .elegant-entry-meta {
                      margin: 4px 0 6px;
                      color: #72698e;
                      font-size: 9pt;
                    }
                    .elegant-entry-link {
                      float: right;
                      color: #72698e;
                      text-decoration: none;
                      font-size: 9pt;
                      font-weight: 600;
                    }
                    .elegant-project-tags {
                      font-size: 0;
                      margin-top: 9px;
                    }
                    .elegant-bottom-grid {
                      width: 100%;
                      display: table;
                      table-layout: fixed;
                      margin-top: 4px;
                    }
                    .elegant-bottom-grid > .elegant-section {
                      display: table-cell;
                      width: 50%;
                      vertical-align: top;
                    }
                    .elegant-bottom-grid > .elegant-section:first-child { padding-right: 16px; }
                    .elegant-bottom-grid > .elegant-section:last-child { padding-left: 16px; }
                  </style>
                </head>
                <body>
                __BODY__
                </body>
                </html>
                """.replace("__BODY__", bodyHtml);
    }

    private String renderElegantAvatar(String imageSource, String fullName) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"elegant-avatar\">");
        if (imageSource != null && !imageSource.isBlank()) {
            html.append("<img src=\"")
                    .append(escape(imageSource))
                    .append("\" alt=\"Profile image\" />");
        } else {
            html.append("<div class=\"elegant-avatar__fallback\"><span>")
                    .append(escape(initials(fullName)))
                    .append("</span></div>");
        }
        html.append("</div>");
        return html.toString();
    }

    private String renderElegantContactSection(String email, String phone, String location, String githubUrl, String linkedInUrl) {
        List<String> rows = new ArrayList<>();
        addSidebarRow(rows, email);
        addSidebarRow(rows, phone);
        addSidebarRow(rows, location);
        addSidebarRow(rows, githubUrl);
        addSidebarRow(rows, linkedInUrl);
        if (rows.isEmpty()) {
            return "";
        }
        return renderElegantSidebarSection("Contact", "<div class=\"elegant-sidebar-stack\">" + String.join("", rows) + "</div>");
    }

    private String renderElegantSkillsSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder("<div class=\"elegant-skill-list\">");
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
                    html.append("<span class=\"elegant-skill-pill\">")
                            .append(escape(name))
                            .append("</span>");
                }
            }
        }
        html.append("</div>");
        if (html.toString().equals("<div class=\"elegant-skill-list\"></div>")) {
            return "";
        }
        return renderElegantSidebarSection(firstNonBlank(section.getTitle(), "Core Skills"), html.toString());
    }

    private String renderElegantEducationSection(CVPdfRenderData.Section section, boolean sidebar) {
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
                entries.append("<div class=\"elegant-sidebar-entry\">")
                        .append("<h4>").append(escape(firstNonBlank(school, "School"))).append("</h4>");
                if (!degree.isBlank() || !fieldOfStudy.isBlank()) {
                    entries.append("<p>").append(escape(joinNonBlank(" - ", degree, fieldOfStudy))).append("</p>");
                }
                if (!dateRange.isBlank()) {
                    entries.append("<span>").append(escape(dateRange)).append("</span>");
                }
                entries.append("</div>");
            } else {
                entries.append("<article class=\"elegant-entry\">")
                        .append("<h3>").append(escape(firstNonBlank(school, "School"))).append("</h3>");
                if (!degree.isBlank() || !fieldOfStudy.isBlank()) {
                    entries.append("<div class=\"elegant-entry-meta\">")
                            .append(escape(joinNonBlank(" - ", degree, fieldOfStudy)))
                            .append("</div>");
                }
                if (!dateRange.isBlank()) {
                    entries.append("<p class=\"elegant-text\">").append(escape(dateRange)).append("</p>");
                }
                entries.append("</article>");
            }
        }
        if (entries.isEmpty()) {
            return "";
        }
        if (sidebar) {
            return renderElegantSidebarSection(firstNonBlank(section.getTitle(), "Education"), entries.toString());
        }
        return renderElegantMainSection(firstNonBlank(section.getTitle(), "Education"), "E", entries.toString());
    }

    private String renderElegantLanguagesSection(CVPdfRenderData.Section section, boolean sidebar) {
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
            entries.append("<div class=\"elegant-language-item\">")
                    .append("<div class=\"elegant-language-head\">")
                    .append("<span class=\"elegant-language-name\">").append(escape(firstNonBlank(name, "Language"))).append("</span>")
                    .append("<span class=\"elegant-language-level\">").append(escape(level.isBlank() ? "Level" : level)).append("</span>")
                    .append("</div>")
                    .append("<div class=\"elegant-language-track\"><span class=\"elegant-language-fill\" style=\"width:")
                    .append(languagePercent(level))
                    .append("%;\"></span></div></div>");
        }
        if (entries.isEmpty()) {
            return "";
        }
        if (sidebar) {
            return renderElegantSidebarSection(firstNonBlank(section.getTitle(), "Languages"), entries.toString());
        }
        return renderElegantMainSection(firstNonBlank(section.getTitle(), "Languages"), "L", entries.toString());
    }

    private String renderElegantTextSection(String title, String marker, String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return renderElegantMainSection(title, marker, "<p class=\"elegant-text\">" + escape(text) + "</p>");
    }

    private String renderElegantExperienceSection(CVPdfRenderData.Section section) {
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
            String location = text(item, "location");
            String summary = text(item, "summary");
            String dateRange = formatDateRange(text(item, "startDate"), text(item, "endDate"), item.path("current").asBoolean(false));
            if (role.isBlank() && company.isBlank() && summary.isBlank()) {
                continue;
            }
            entries.append("<article class=\"elegant-entry\">")
                    .append("<div class=\"elegant-entry-head\"><h3>")
                    .append(escape(firstNonBlank(role, "Role")))
                    .append("</h3>");
            if (!dateRange.isBlank()) {
                entries.append("<span class=\"elegant-entry-date\">").append(escape(dateRange)).append("</span>");
            }
            entries.append("</div>");
            if (!company.isBlank() || !location.isBlank()) {
                entries.append("<div class=\"elegant-entry-meta\">")
                        .append(escape(joinNonBlank(" - ", company, location)))
                        .append("</div>");
            }
            if (!summary.isBlank()) {
                entries.append("<p class=\"elegant-text\">").append(escape(summary)).append("</p>");
            }
            entries.append("</article>");
        }
        if (entries.isEmpty()) {
            return "";
        }
        return renderElegantMainSection(firstNonBlank(section.getTitle(), "Experience"), "E", entries.toString());
    }

    private String renderElegantProjectsSection(CVPdfRenderData.Section section) {
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
            entries.append("<article class=\"elegant-entry\">")
                    .append("<div class=\"elegant-entry-head\"><h3>")
                    .append(escape(firstNonBlank(title, "Untitled Project")))
                    .append("</h3>");
            if (!projectUrl.isBlank()) {
                entries.append("<a class=\"elegant-entry-link\" href=\"")
                        .append(escape(projectUrl))
                        .append("\">Open</a>");
            }
            entries.append("</div>");
            if (!description.isBlank()) {
                entries.append("<p class=\"elegant-text\">").append(escape(description)).append("</p>");
            } else {
                entries.append("<p class=\"elegant-text\">Project description missing</p>");
            }
            if (!skillNames.isEmpty()) {
                entries.append("<div class=\"elegant-project-tags\">");
                for (String skillName : skillNames) {
                    entries.append("<span class=\"elegant-project-pill\">").append(escape(skillName)).append("</span>");
                }
                entries.append("</div>");
            }
            entries.append("</article>");
        }
        if (entries.isEmpty()) {
            return "";
        }
        return renderElegantMainSection(firstNonBlank(section.getTitle(), "Selected Project"), "S", entries.toString());
    }

    private String renderElegantSidebarSection(String title, String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return "";
        }
        return "<section class=\"elegant-sidebar-section\"><div class=\"elegant-sidebar-heading\">"
                + "<span class=\"elegant-sidebar-rule\"></span>"
                + "<h3>" + escape(title) + "</h3>"
                + "<span class=\"elegant-sidebar-rule\"></span>"
                + "</div>"
                + contentHtml
                + "</section>";
    }

    private String renderElegantMainSection(String title, String marker, String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return "";
        }
        return "<section class=\"elegant-section\"><div class=\"elegant-section-heading\">"
                + "<span class=\"elegant-section-badge\">" + escape(marker) + "</span>"
                + "<span class=\"elegant-section-title\">" + escape(title) + "</span>"
                + "<span class=\"elegant-section-line\"></span>"
                + "</div>"
                + contentHtml
                + "</section>";
    }

    private void addSidebarRow(List<String> rows, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        rows.add("<p>" + escape(value) + "</p>");
    }

    private void addHeaderContact(List<String> contacts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        contacts.add("<span class=\"elegant-contact-chip\">" + escape(value) + "</span>");
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
