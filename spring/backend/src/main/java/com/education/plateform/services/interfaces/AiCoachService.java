package com.education.plateform.services.interfaces;

import com.education.plateform.dto.AiCoachPreviewRequest;
import com.education.plateform.dto.AiCoachPreviewResponse;
import com.education.plateform.entities.Application;

public interface AiCoachService {
    AiCoachPreviewResponse preview(AiCoachPreviewRequest request);
    Application applyImprovedText(Long applicationId, Long applicantId, String improvedText);
}
