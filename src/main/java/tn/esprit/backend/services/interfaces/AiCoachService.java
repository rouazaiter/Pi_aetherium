package tn.esprit.backend.services.interfaces;

import tn.esprit.backend.dto.AiCoachPreviewRequest;
import tn.esprit.backend.dto.AiCoachPreviewResponse;
import tn.esprit.backend.entities.Application;

public interface AiCoachService {
    AiCoachPreviewResponse preview(AiCoachPreviewRequest request);
    Application applyImprovedText(Long applicationId, Long applicantId, String improvedText);
}
