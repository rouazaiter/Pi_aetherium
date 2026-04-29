package com.education.platform.services.implementations.cv;

abstract class PlaceholderCvPdfRenderer extends AbstractHtmlCvPdfRenderer {

    public String renderHtml(CVPdfRenderData data) {
        StringBuilder sections = new StringBuilder();
        for (CVPdfRenderData.Section section : visibleSections(data)) {
            sections.append("<li>")
                    .append(escape(section.getType() == null ? "SECTION" : section.getType().name()))
                    .append("</li>");
        }

        String body = """
                <div class="cv-root">
                  <header class="cv-header">
                    <div class="cv-name">%s Template</div>
                    <div class="cv-headline">PDF export wiring is active.</div>
                    <div class="cv-placeholder-note">
                      The %s renderer is scaffolded, but the final design is not implemented yet.
                    </div>
                  </header>
                  <section class="cv-section">
                    <div class="cv-section-title">Visible Sections</div>
                    <ul>%s</ul>
                  </section>
                </div>
                """.formatted(escape(themeTitle()), escape(themeTitle()), sections);

        return renderDocument("CV " + themeTitle(), "theme-" + themeTitle().toLowerCase(), body);
    }

    protected abstract String themeTitle();
}
