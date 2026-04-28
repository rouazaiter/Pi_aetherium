package com.education.plateform.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.education.plateform.dto.AiCoachApplyRequest;
import com.education.plateform.dto.AiCoachPreviewRequest;
import com.education.plateform.dto.AiCoachPreviewResponse;
import com.education.plateform.entities.Application;
import com.education.plateform.services.interfaces.AiCoachService;

@RestController
@RequestMapping("/api/ai/coach")
@RequiredArgsConstructor
public class AiCoachController {

    private final AiCoachService aiCoachService;

    @PostMapping("/preview")
    public ResponseEntity<AiCoachPreviewResponse> preview(@Valid @RequestBody AiCoachPreviewRequest request) {
        return ResponseEntity.ok(aiCoachService.preview(request));
    }

    @PostMapping("/apply/{applicationId}/{applicant-id}")
    public ResponseEntity<Application> apply(
            @PathVariable Long applicationId,
            @PathVariable("applicant-id") Long applicantId,
            @Valid @RequestBody AiCoachApplyRequest request
    ) {
        return ResponseEntity.ok(aiCoachService.applyImprovedText(applicationId, applicantId, request.improvedText()));
    }
}
