package tn.esprit.backend.dto;

import java.util.List;

public record AiCoachPreviewResponse(
        String improvedText,
        int relevanceScore,
        int clarityScore,
        List<String> missingPoints,
        List<String> suggestions,
        String changesSummary,
        boolean generatedByAi
) {
}
