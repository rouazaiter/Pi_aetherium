package com.education.platform.services.interfaces.cv;

import com.education.platform.services.implementations.cv.CVPdfRenderData;

public interface CVPdfRenderer {

    boolean supports(String theme);

    String renderHtml(CVPdfRenderData data);
}
