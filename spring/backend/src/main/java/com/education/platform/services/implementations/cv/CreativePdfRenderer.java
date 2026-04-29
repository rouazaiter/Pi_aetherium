package com.education.platform.services.implementations.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.services.interfaces.cv.CVPdfRenderer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class CreativePdfRenderer extends AbstractHtmlCvPdfRenderer implements CVPdfRenderer {

    private static final String THEME = "CREATIVE";

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
        body.append("<div class=\"creative-root\"><div class=\"creative-layout\">");
        body.append("<div class=\"creative-sidebar-curve\"></div>");
        body.append("<div class=\"creative-bottom-wave\"></div>");

        body.append("<aside class=\"creative-sidebar\">");
        body.append(renderCreativeAvatar(imageSource, fullName, headline));
        body.append(renderCreativeContactSection(email, phone, location, githubUrl, linkedInUrl));
        body.append(renderCreativeEducationSection(educationSection));
        body.append(renderCreativeLanguagesSection(languagesSection));
        body.append("</aside>");

        body.append("<main class=\"creative-main\">");
        body.append("<div class=\"creative-top-wave\"></div>");
        body.append("<div class=\"creative-dot-grid creative-dot-grid--top\"></div>");
        body.append("<div class=\"creative-dot-grid creative-dot-grid--bottom\"></div>");
        body.append("<div class=\"creative-orbit\"></div>");
        body.append("<header class=\"creative-header\">");
        body.append("<h1 class=\"creative-name\">").append(escape(fullName)).append("</h1>");
        body.append("<div class=\"creative-headline\">").append(escape(headline)).append("</div>");
        body.append("<div class=\"creative-headline-line\"></div>");

        List<String> headerContacts = new ArrayList<>();
        addHeaderContact(headerContacts, email);
        addHeaderContact(headerContacts, phone);
        addHeaderContact(headerContacts, location);
        if (!headerContacts.isEmpty()) {
            body.append("<div class=\"creative-contact-row\">")
                    .append(String.join("", headerContacts))
                    .append("</div>");
        }
        body.append("</header>");

        if (profileSection != null && profileSection.isVisible() && !summary.isBlank()) {
            body.append(renderCreativeMainCard("P", "Summary",
                    "<p class=\"creative-text\">" + escape(summary) + "</p>"));
        }

        body.append(renderCreativeProjectsSection(projectsSection));
        body.append(renderCreativeSkillsSection(skillsSection));
        body.append(renderCreativeExperienceSection(experienceSection));
        body.append("</main></div></div>");

        return renderCreativeDocument(body.toString());
    }

    private String renderCreativeDocument(String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page { size: A4; margin: 10mm; }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      font-family: Arial, Helvetica, sans-serif;
                      color: #24306d;
                      background: #ffffff;
                    }
                    h1, h2, h3, h4, p { margin: 0; }
                    .creative-root {
                      width: 100%;
                      min-height: 100%;
                      padding: 8px;
                      background:
                        radial-gradient(circle at top right, rgba(224, 217, 255, 0.75), transparent 22%),
                        linear-gradient(180deg, #fcfbff 0%, #ffffff 75%, #faf9ff 100%);
                    }
                    .creative-layout {
                      position: relative;
                      overflow: hidden;
                      width: 100%;
                      display: table;
                      table-layout: fixed;
                      border-radius: 24px;
                      background:
                        radial-gradient(circle at top left, rgba(119, 90, 255, 0.12), transparent 18%),
                        linear-gradient(180deg, #fdfcff 0%, #ffffff 100%);
                      border: 1px solid #ece6ff;
                    }
                    .creative-sidebar,
                    .creative-main {
                      display: table-cell;
                      vertical-align: top;
                      position: relative;
                      z-index: 1;
                    }
                    .creative-sidebar {
                      width: 31%;
                      padding: 22px 18px 26px;
                    }
                    .creative-main {
                      width: 69%;
                      padding: 20px 20px 24px 8px;
                    }
                    .creative-sidebar-curve,
                    .creative-bottom-wave,
                    .creative-top-wave,
                    .creative-dot-grid,
                    .creative-orbit {
                      position: absolute;
                      pointer-events: none;
                    }
                    .creative-sidebar-curve {
                      left: -40px;
                      top: -16px;
                      width: 250px;
                      height: 210px;
                      border-radius: 0 0 140px 0;
                      background: linear-gradient(180deg, #3322a2 0%, #5b3ed8 78%, rgba(91, 62, 216, 0.84) 100%);
                    }
                    .creative-bottom-wave {
                      right: -34px;
                      bottom: -34px;
                      width: 250px;
                      height: 150px;
                      border-radius: 140px 0 0 0;
                      background: linear-gradient(160deg, rgba(75, 58, 190, 0.95) 0%, rgba(104, 78, 243, 0.94) 100%);
                    }
                    .creative-top-wave {
                      top: -10px;
                      right: 0;
                      width: 250px;
                      height: 110px;
                      border-radius: 0 0 0 100px;
                      background:
                        radial-gradient(circle at top right, rgba(216, 204, 255, 0.9), transparent 58%),
                        linear-gradient(180deg, rgba(247, 244, 255, 0.96), rgba(255, 255, 255, 0));
                    }
                    .creative-dot-grid {
                      width: 44px;
                      height: 44px;
                      background-size: 11px 11px;
                    }
                    .creative-dot-grid--top {
                      top: 20px;
                      right: 24px;
                      background-image: radial-gradient(circle, rgba(101, 79, 226, 0.9) 2px, transparent 2.2px);
                    }
                    .creative-dot-grid--bottom {
                      right: 8px;
                      bottom: 100px;
                      background-image: radial-gradient(circle, rgba(214, 176, 109, 0.76) 1.6px, transparent 1.8px);
                    }
                    .creative-orbit {
                      top: 78px;
                      right: 132px;
                      width: 42px;
                      height: 42px;
                      border-radius: 50%;
                      border: 1.5px solid rgba(215, 169, 94, 0.96);
                    }
                    .creative-avatar-wrap {
                      position: relative;
                      width: 196px;
                      height: 196px;
                      margin: 2px 0 14px 8px;
                    }
                    .creative-avatar {
                      position: absolute;
                      inset: 17px;
                      padding: 6px;
                      border-radius: 50%;
                      background: rgba(255, 255, 255, 0.96);
                      box-shadow: 0 18px 36px rgba(99, 77, 213, 0.18);
                    }
                    .creative-avatar img,
                    .creative-avatar__fallback {
                      width: 100%;
                      height: 100%;
                      border-radius: 50%;
                    }
                    .creative-avatar img {
                      display: block;
                      object-fit: cover;
                    }
                    .creative-avatar__fallback {
                      display: table;
                      background: linear-gradient(135deg, #7656ff 0%, #ca80ff 100%);
                      color: #ffffff;
                      font-size: 34px;
                      font-weight: 700;
                      text-align: center;
                    }
                    .creative-avatar__fallback span {
                      display: table-cell;
                      vertical-align: middle;
                    }
                    .creative-avatar-ring,
                    .creative-avatar-dot {
                      position: absolute;
                      border-radius: 50%;
                    }
                    .creative-avatar-ring--outer {
                      inset: 0;
                      border: 2px solid rgba(105, 74, 245, 0.85);
                    }
                    .creative-avatar-ring--mid {
                      inset: 8px;
                      border: 10px solid rgba(255, 255, 255, 0.9);
                    }
                    .creative-avatar-ring--accent {
                      width: 86px;
                      height: 86px;
                      left: -18px;
                      top: -18px;
                      border-top: 2px solid rgba(222, 181, 98, 0.98);
                      border-left: 2px solid rgba(222, 181, 98, 0.98);
                      border-right: 2px solid transparent;
                      border-bottom: 2px solid transparent;
                      transform: rotate(-14deg);
                    }
                    .creative-avatar-dot {
                      right: 8px;
                      bottom: 24px;
                      width: 40px;
                      height: 40px;
                      background: linear-gradient(135deg, #7b57ff 0%, #5e44de 100%);
                    }
                    .creative-tagline {
                      margin: 0 0 18px 12px;
                      max-width: 160px;
                      color: #6958de;
                      font-size: 16px;
                      line-height: 1.35;
                      font-style: italic;
                    }
                    .creative-contact-card {
                      padding: 18px 16px;
                      margin: 0 8px 18px;
                      border-radius: 18px;
                      color: #ffffff;
                      background: linear-gradient(135deg, #211968 0%, #312480 56%, #4733a3 100%);
                      box-shadow: 0 18px 34px rgba(38, 28, 117, 0.22);
                    }
                    .creative-contact-row-head,
                    .creative-side-heading,
                    .creative-card-head {
                      width: 100%;
                    }
                    .creative-contact-row-head::after,
                    .creative-side-heading::after,
                    .creative-card-head::after,
                    .creative-language-head::after,
                    .creative-entry-head::after,
                    .creative-project-title::after {
                      content: "";
                      display: block;
                      clear: both;
                    }
                    .creative-contact-badge,
                    .creative-side-icon,
                    .creative-card-icon {
                      display: block;
                      float: left;
                      width: 34px;
                      height: 34px;
                      border-radius: 50%;
                      text-align: center;
                      line-height: 34px;
                      font-weight: 700;
                    }
                    .creative-contact-badge {
                      color: #ffffff;
                      background: rgba(255, 255, 255, 0.18);
                    }
                    .creative-side-icon,
                    .creative-card-icon {
                      color: #5f45dd;
                      background: #efe9ff;
                    }
                    .creative-contact-title,
                    .creative-side-title,
                    .creative-card-title {
                      display: block;
                      float: left;
                      margin-left: 10px;
                      font-size: 11pt;
                      font-weight: 700;
                      letter-spacing: 0.14em;
                      text-transform: uppercase;
                    }
                    .creative-contact-title {
                      color: #ffffff;
                      line-height: 34px;
                    }
                    .creative-side-title,
                    .creative-card-title {
                      color: #5f45dd;
                      line-height: 34px;
                    }
                    .creative-contact-line,
                    .creative-side-line,
                    .creative-card-line {
                      display: block;
                      overflow: hidden;
                      height: 1px;
                      margin-top: 17px;
                      margin-left: 12px;
                    }
                    .creative-contact-line {
                      background: rgba(255, 255, 255, 0.28);
                    }
                    .creative-side-line,
                    .creative-card-line {
                      background: linear-gradient(90deg, rgba(108, 92, 231, 0.24), rgba(108, 92, 231, 0.06));
                    }
                    .creative-contact-body {
                      margin-top: 12px;
                    }
                    .creative-contact-item,
                    .creative-side-entry p,
                    .creative-side-entry span {
                      color: rgba(255, 255, 255, 0.94);
                      font-size: 9.4pt;
                      line-height: 1.5;
                      word-break: break-word;
                    }
                    .creative-contact-item + .creative-contact-item,
                    .creative-side-entry + .creative-side-entry,
                    .creative-language-item + .creative-language-item {
                      margin-top: 10px;
                    }
                    .creative-side-section {
                      margin: 0 8px 18px;
                      page-break-inside: avoid;
                    }
                    .creative-side-stack,
                    .creative-language-list {
                      margin-top: 12px;
                    }
                    .creative-side-entry h4 {
                      margin-bottom: 4px;
                      color: #24306d;
                      font-size: 10pt;
                      font-weight: 700;
                    }
                    .creative-side-entry p,
                    .creative-side-entry span {
                      display: block;
                      color: #61668f;
                    }
                    .creative-language-name {
                      float: left;
                      color: #24306d;
                      font-weight: 700;
                    }
                    .creative-language-level {
                      float: right;
                      color: #6d6ea0;
                      font-size: 8.8pt;
                      font-weight: 700;
                    }
                    .creative-language-dots {
                      margin-top: 6px;
                    }
                    .creative-language-dot {
                      display: inline-block;
                      width: 8px;
                      height: 8px;
                      margin-right: 6px;
                      border-radius: 50%;
                      background: rgba(124, 102, 228, 0.24);
                    }
                    .creative-language-dot--active {
                      background: linear-gradient(135deg, #6d54ef 0%, #8b64ff 100%);
                    }
                    .creative-header {
                      position: relative;
                      padding: 18px 0 8px;
                    }
                    .creative-name {
                      max-width: 13ch;
                      color: #23285f;
                      font-size: 38px;
                      line-height: 0.92;
                      font-weight: 700;
                      letter-spacing: -0.05em;
                      text-transform: uppercase;
                      font-family: Georgia, "Times New Roman", serif;
                    }
                    .creative-headline {
                      margin-top: 12px;
                      color: #5c43dc;
                      font-size: 18px;
                      font-weight: 700;
                      letter-spacing: 0.28em;
                      text-transform: uppercase;
                    }
                    .creative-headline-line {
                      width: 78px;
                      height: 2px;
                      margin-top: 10px;
                      background: linear-gradient(90deg, #c49445 0%, #e4c48a 100%);
                    }
                    .creative-contact-row {
                      margin-top: 12px;
                      color: #515883;
                      font-size: 9.6pt;
                    }
                    .creative-contact-chip {
                      display: inline-block;
                      margin-right: 12px;
                      margin-bottom: 5px;
                    }
                    .creative-card {
                      position: relative;
                      margin-top: 14px;
                      padding: 18px 18px 16px;
                      border-radius: 22px;
                      background: rgba(255, 255, 255, 0.94);
                      border: 1px solid rgba(232, 226, 255, 0.96);
                      box-shadow: 0 18px 34px rgba(118, 91, 214, 0.11);
                      page-break-inside: avoid;
                    }
                    .creative-text {
                      margin-top: 12px;
                      color: #52597e;
                      font-size: 10.2pt;
                      line-height: 1.65;
                      white-space: pre-line;
                    }
                    .creative-project-list,
                    .creative-experience-list {
                      margin-top: 12px;
                    }
                    .creative-project + .creative-project,
                    .creative-experience + .creative-experience {
                      margin-top: 12px;
                      padding-top: 12px;
                      border-top: 1px solid rgba(236, 230, 255, 0.92);
                    }
                    .creative-project {
                      width: 100%;
                    }
                    .creative-project-content {
                      width: 68%;
                      display: inline-block;
                      vertical-align: top;
                      padding-right: 10px;
                    }
                    .creative-project-art {
                      width: 30%;
                      display: inline-block;
                      vertical-align: top;
                      height: 110px;
                      margin-left: 2%;
                      border-radius: 18px;
                      overflow: hidden;
                      background:
                        radial-gradient(circle at 70% 28%, rgba(223, 212, 255, 0.96), transparent 46%),
                        linear-gradient(135deg, rgba(248, 245, 255, 0.96) 0%, rgba(237, 230, 255, 0.94) 100%);
                      position: relative;
                    }
                    .creative-project-art::before,
                    .creative-project-art::after,
                    .creative-project-art i {
                      content: "";
                      position: absolute;
                      display: block;
                    }
                    .creative-project-art::before {
                      left: 16px;
                      bottom: 16px;
                      width: 26px;
                      height: 44px;
                      border-radius: 10px;
                      background: linear-gradient(180deg, #a58cff 0%, #7d61ff 100%);
                      box-shadow: 34px 0 0 rgba(126, 101, 255, 0.75), 68px 0 0 rgba(126, 101, 255, 0.54);
                    }
                    .creative-project-art::after {
                      right: 16px;
                      top: 16px;
                      width: 34px;
                      height: 34px;
                      border-radius: 50%;
                      background: radial-gradient(circle, rgba(206, 176, 255, 0.96), rgba(146, 118, 255, 0.8));
                    }
                    .creative-project-art i {
                      left: 22px;
                      top: 18px;
                      width: 64px;
                      height: 36px;
                      border-radius: 12px;
                      background: rgba(255, 255, 255, 0.86);
                    }
                    .creative-project-title h3,
                    .creative-entry-head h3 {
                      float: left;
                      color: #23295f;
                      font-size: 12pt;
                      font-weight: 700;
                    }
                    .creative-project-link,
                    .creative-entry-date {
                      float: right;
                      color: #6b52f5;
                      font-size: 8.8pt;
                      font-weight: 700;
                      text-decoration: none;
                    }
                    .creative-project-tags,
                    .creative-skill-list {
                      font-size: 0;
                      margin-top: 12px;
                    }
                    .creative-project-pill,
                    .creative-skill-pill {
                      display: inline-block;
                      margin: 0 7px 7px 0;
                      padding: 7px 14px;
                      border-radius: 999px;
                      font-size: 8.8pt;
                      font-weight: 700;
                      color: #35407e;
                      border: 1px solid rgba(199, 186, 241, 0.88);
                      background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(245, 240, 255, 0.96) 100%);
                    }
                    .creative-skill-pill--active {
                      color: #ffffff;
                      border-color: transparent;
                      background: linear-gradient(135deg, #7a56ff 0%, #5d45db 100%);
                    }
                    .creative-entry-meta {
                      margin-top: 5px;
                      color: #676d98;
                      font-size: 9pt;
                      font-weight: 600;
                    }
                  </style>
                </head>
                <body>
                  {{BODY}}
                </body>
                </html>
                """.replace("{{BODY}}", bodyHtml);
    }

    private String renderCreativeAvatar(String imageSource, String fullName, String headline) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"creative-avatar-wrap\">")
                .append("<div class=\"creative-avatar-ring creative-avatar-ring--outer\"></div>")
                .append("<div class=\"creative-avatar-ring creative-avatar-ring--mid\"></div>")
                .append("<div class=\"creative-avatar-ring creative-avatar-ring--accent\"></div>")
                .append("<div class=\"creative-avatar-dot\"></div>")
                .append("<div class=\"creative-avatar\">");
        if (imageSource != null && !imageSource.isBlank()) {
            html.append("<img src=\"")
                    .append(escape(imageSource))
                    .append("\" alt=\"Profile image\" />");
        } else {
            html.append("<div class=\"creative-avatar__fallback\"><span>")
                    .append(escape(initials(fullName)))
                    .append("</span></div>");
        }
        html.append("</div></div>")
                .append("<div class=\"creative-tagline\">")
                .append(escape(firstNonBlank(headline, "Creative professional building standout work.")))
                .append("</div>");
        return html.toString();
    }

    private String renderCreativeContactSection(String email, String phone, String location, String githubUrl, String linkedInUrl) {
        List<String> rows = new ArrayList<>();
        addSidebarContact(rows, email);
        addSidebarContact(rows, phone);
        addSidebarContact(rows, location);
        addSidebarContact(rows, githubUrl);
        addSidebarContact(rows, linkedInUrl);
        if (rows.isEmpty()) {
            return "";
        }
        return "<section class=\"creative-contact-card\"><div class=\"creative-contact-row-head\">"
                + "<span class=\"creative-contact-badge\">C</span>"
                + "<span class=\"creative-contact-title\">Contact</span>"
                + "<span class=\"creative-contact-line\"></span></div>"
                + "<div class=\"creative-contact-body\">"
                + String.join("", rows)
                + "</div></section>";
    }

    private String renderCreativeEducationSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder("<div class=\"creative-side-stack\">");
        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String school = text(item, "school");
            String degree = text(item, "degree");
            String fieldOfStudy = text(item, "fieldOfStudy");
            String location = text(item, "location");
            String dateRange = formatDateRange(text(item, "startDate"), text(item, "endDate"), item.path("current").asBoolean(false));
            if (school.isBlank() && degree.isBlank() && fieldOfStudy.isBlank() && location.isBlank() && dateRange.isBlank()) {
                continue;
            }
            entries.append("<div class=\"creative-side-entry\">")
                    .append("<h4>").append(escape(firstNonBlank(school, "School"))).append("</h4>");
            if (!degree.isBlank() || !fieldOfStudy.isBlank()) {
                entries.append("<p>").append(escape(joinNonBlank(" - ", degree, fieldOfStudy))).append("</p>");
            }
            if (!location.isBlank()) {
                entries.append("<p>").append(escape(location)).append("</p>");
            }
            if (!dateRange.isBlank()) {
                entries.append("<span>").append(escape(dateRange)).append("</span>");
            }
            entries.append("</div>");
        }
        entries.append("</div>");
        if (entries.toString().equals("<div class=\"creative-side-stack\"></div>")) {
            return "";
        }
        return renderCreativeSideSection(firstNonBlank(section.getTitle(), "Education"), "E", entries.toString());
    }

    private String renderCreativeLanguagesSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder("<div class=\"creative-language-list\">");
        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String name = text(item, "name");
            String level = text(item, "proficiency");
            if (name.isBlank() && level.isBlank()) {
                continue;
            }
            entries.append("<div class=\"creative-language-item\">")
                    .append("<div class=\"creative-language-head\">")
                    .append("<span class=\"creative-language-name\">").append(escape(firstNonBlank(name, "Language"))).append("</span>")
                    .append("<span class=\"creative-language-level\">").append(escape(level.isBlank() ? "Level" : level)).append("</span>")
                    .append("</div><div class=\"creative-language-dots\">");
            int dots = languageDots(level);
            for (int index = 1; index <= 5; index++) {
                entries.append("<span class=\"creative-language-dot")
                        .append(index <= dots ? " creative-language-dot--active" : "")
                        .append("\"></span>");
            }
            entries.append("</div></div>");
        }
        entries.append("</div>");
        if (entries.toString().equals("<div class=\"creative-language-list\"></div>")) {
            return "";
        }
        return renderCreativeSideSection(firstNonBlank(section.getTitle(), "Languages"), "L", entries.toString());
    }

    private String renderCreativeProjectsSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder("<div class=\"creative-project-list\">");
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
            entries.append("<article class=\"creative-project\">")
                    .append("<div class=\"creative-project-content\">")
                    .append("<div class=\"creative-project-title\"><h3>")
                    .append(escape(firstNonBlank(title, "Untitled Project")))
                    .append("</h3>");
            if (!projectUrl.isBlank()) {
                entries.append("<a class=\"creative-project-link\" href=\"")
                        .append(escape(projectUrl))
                        .append("\">Open</a>");
            }
            entries.append("</div>");
            if (!description.isBlank()) {
                entries.append("<p class=\"creative-text\">").append(escape(description)).append("</p>");
            } else {
                entries.append("<p class=\"creative-text\">Project description missing</p>");
            }
            if (!skillNames.isEmpty()) {
                entries.append("<div class=\"creative-project-tags\">");
                for (String skillName : skillNames) {
                    entries.append("<span class=\"creative-project-pill\">").append(escape(skillName)).append("</span>");
                }
                entries.append("</div>");
            }
            entries.append("</div><div class=\"creative-project-art\"><i></i></div></article>");
        }
        entries.append("</div>");
        if (entries.toString().equals("<div class=\"creative-project-list\"></div>")) {
            return "";
        }
        return renderCreativeMainCard("R", firstNonBlank(section.getTitle(), "Projects"), entries.toString());
    }

    private String renderCreativeSkillsSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder("<div class=\"creative-skill-list\">");
        boolean first = true;
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
                    html.append("<span class=\"creative-skill-pill");
                    if (first) {
                        html.append(" creative-skill-pill--active");
                        first = false;
                    }
                    html.append("\">")
                            .append(escape(name))
                            .append("</span>");
                }
            }
        }
        html.append("</div>");
        if (html.toString().equals("<div class=\"creative-skill-list\"></div>")) {
            return "";
        }
        return renderCreativeMainCard("S", firstNonBlank(section.getTitle(), "Skills"), html.toString());
    }

    private String renderCreativeExperienceSection(CVPdfRenderData.Section section) {
        JsonNode content = section == null ? null : section.getContent();
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        StringBuilder entries = new StringBuilder("<div class=\"creative-experience-list\">");
        for (JsonNode item : content) {
            if (item == null || item.isNull()) {
                continue;
            }
            String role = text(item, "role");
            String company = text(item, "company");
            String location = text(item, "location");
            String summary = text(item, "summary");
            String dateRange = formatDateRange(text(item, "startDate"), text(item, "endDate"), item.path("current").asBoolean(false));
            if (role.isBlank() && company.isBlank() && summary.isBlank() && location.isBlank()) {
                continue;
            }
            entries.append("<article class=\"creative-experience\">")
                    .append("<div class=\"creative-entry-head\"><h3>")
                    .append(escape(firstNonBlank(role, "Role")))
                    .append("</h3>");
            if (!dateRange.isBlank()) {
                entries.append("<span class=\"creative-entry-date\">").append(escape(dateRange)).append("</span>");
            }
            entries.append("</div>");
            if (!company.isBlank() || !location.isBlank()) {
                entries.append("<div class=\"creative-entry-meta\">")
                        .append(escape(joinNonBlank(" - ", company, location)))
                        .append("</div>");
            }
            if (!summary.isBlank()) {
                entries.append("<p class=\"creative-text\">").append(escape(summary)).append("</p>");
            }
            entries.append("</article>");
        }
        entries.append("</div>");
        if (entries.toString().equals("<div class=\"creative-experience-list\"></div>")) {
            return "";
        }
        return renderCreativeMainCard("X", firstNonBlank(section.getTitle(), "Experience"), entries.toString());
    }

    private String renderCreativeSideSection(String title, String marker, String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return "";
        }
        return "<section class=\"creative-side-section\"><div class=\"creative-side-heading\">"
                + "<span class=\"creative-side-icon\">" + escape(marker) + "</span>"
                + "<span class=\"creative-side-title\">" + escape(title) + "</span>"
                + "<span class=\"creative-side-line\"></span>"
                + "</div>"
                + contentHtml
                + "</section>";
    }

    private String renderCreativeMainCard(String marker, String title, String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return "";
        }
        return "<section class=\"creative-card\"><div class=\"creative-card-head\">"
                + "<span class=\"creative-card-icon\">" + escape(marker) + "</span>"
                + "<span class=\"creative-card-title\">" + escape(title) + "</span>"
                + "<span class=\"creative-card-line\"></span>"
                + "</div>"
                + contentHtml
                + "</section>";
    }

    private void addSidebarContact(List<String> contacts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        contacts.add("<p class=\"creative-contact-item\">" + escape(value) + "</p>");
    }

    private void addHeaderContact(List<String> contacts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        contacts.add("<span class=\"creative-contact-chip\">" + escape(value) + "</span>");
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

    private int languageDots(String proficiency) {
        String normalized = proficiency == null ? "" : proficiency.trim().toUpperCase();
        return switch (normalized) {
            case "A1" -> 1;
            case "A2" -> 2;
            case "B1" -> 3;
            case "B2" -> 4;
            case "C1", "C2" -> 5;
            default -> 3;
        };
    }
}
