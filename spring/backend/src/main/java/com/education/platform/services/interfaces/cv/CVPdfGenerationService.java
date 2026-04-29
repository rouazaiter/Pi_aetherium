package com.education.platform.services.interfaces.cv;

public interface CVPdfGenerationService {

    byte[] generatePdf(Long userId, Long draftId);

    String resolveFilename(Long userId, Long draftId);
}
