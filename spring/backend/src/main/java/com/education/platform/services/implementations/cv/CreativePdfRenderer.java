package com.education.platform.services.implementations.cv;

import com.education.platform.services.interfaces.cv.CVPdfRenderer;
import org.springframework.stereotype.Component;

@Component
public class CreativePdfRenderer extends PlaceholderCvPdfRenderer implements CVPdfRenderer {

    @Override
    public boolean supports(String theme) {
        return "CREATIVE".equalsIgnoreCase(theme);
    }

    @Override
    protected String themeTitle() {
        return "Creative";
    }
}
