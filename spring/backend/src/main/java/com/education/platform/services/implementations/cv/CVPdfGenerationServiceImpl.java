package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVSection;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.services.interfaces.cv.CVPdfGenerationService;
import com.education.platform.services.interfaces.cv.CVPdfRenderer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class CVPdfGenerationServiceImpl implements CVPdfGenerationService {

    private final CVDraftRepository cvDraftRepository;
    private final List<CVPdfRenderer> renderers;
    private final ObjectMapper objectMapper;

    public CVPdfGenerationServiceImpl(
            CVDraftRepository cvDraftRepository,
            List<CVPdfRenderer> renderers,
            ObjectMapper objectMapper) {
        this.cvDraftRepository = cvDraftRepository;
        this.renderers = renderers;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long userId, Long draftId) {
        CVDraft draft = loadDraft(userId, draftId);
        CVPdfRenderer renderer = resolveRenderer(draft.getTheme());
        String html = renderer.renderHtml(toRenderData(draft));

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate CV PDF");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveFilename(Long userId, Long draftId) {
        CVDraft draft = loadDraft(userId, draftId);
        return "cv-" + sanitizeThemeForFilename(draft.getTheme()) + ".pdf";
    }

    private CVDraft loadDraft(Long userId, Long draftId) {
        return cvDraftRepository.findByIdAndUser_Id(draftId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV draft not found"));
    }

    private CVPdfRenderer resolveRenderer(String theme) {
        String normalizedTheme = normalizeTheme(theme);
        return renderers.stream()
                .filter(renderer -> renderer.supports(normalizedTheme))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_IMPLEMENTED, "CV template not implemented yet"));
    }

    private CVPdfRenderData toRenderData(CVDraft draft) {
        return CVPdfRenderData.builder()
                .userId(draft.getUser().getId())
                .draftId(draft.getId())
                .theme(normalizeTheme(draft.getTheme()))
                .settings(readJson(draft.getSettingsJson()))
                .sections(draft.getSections().stream().map(this::toSection).toList())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    private CVPdfRenderData.Section toSection(CVSection section) {
        return CVPdfRenderData.Section.builder()
                .type(section.getType())
                .title(section.getTitle())
                .orderIndex(section.getOrderIndex())
                .visible(Boolean.TRUE.equals(section.getVisible()))
                .content(readJson(section.getContentJson()))
                .build();
    }

    private JsonNode readJson(String value) {
        try {
            return value == null ? objectMapper.nullNode() : objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read CV draft content");
        }
    }

    private String normalizeTheme(String theme) {
        return theme == null || theme.isBlank() ? CVDraftServiceImpl.DEFAULT_THEME : theme.trim().toUpperCase();
    }

    private String sanitizeThemeForFilename(String theme) {
        return normalizeTheme(theme).toLowerCase().replace('_', '-');
    }
}
