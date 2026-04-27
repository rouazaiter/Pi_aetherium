package com.education.platform.controllers.cv;

import com.education.platform.dto.cv.CVDraftResponse;
import com.education.platform.dto.cv.CvAiChatRequest;
import com.education.platform.dto.cv.CvAiChatResponse;
import com.education.platform.dto.cv.CvAiImproveRequest;
import com.education.platform.dto.cv.CvAiImproveResponse;
import com.education.platform.dto.cv.CvAiJobMatchRequest;
import com.education.platform.dto.cv.CvAiJobMatchResponse;
import com.education.platform.dto.cv.CVPreviewOptions;
import com.education.platform.dto.cv.CVPreviewResponse;
import com.education.platform.dto.cv.CVProfileResponse;
import com.education.platform.dto.cv.UpdateCVDraftRequest;
import com.education.platform.dto.cv.UpdateCVProfileRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.cv.CVAiAssistantService;
import com.education.platform.services.interfaces.cv.CVAiJobMatchService;
import com.education.platform.services.interfaces.cv.CVAiImprovementService;
import com.education.platform.services.interfaces.cv.CVBuilderService;
import com.education.platform.services.interfaces.cv.CVDraftService;
import com.education.platform.services.interfaces.cv.CVProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cv/me")
public class CVController {

    private final CurrentUserService currentUserService;
    private final CVProfileService cvProfileService;
    private final CVBuilderService cvBuilderService;
    private final CVDraftService cvDraftService;
    private final CVAiImprovementService cvAiImprovementService;
    private final CVAiAssistantService cvAiAssistantService;
    private final CVAiJobMatchService cvAiJobMatchService;

    public CVController(
            CurrentUserService currentUserService,
            CVProfileService cvProfileService,
            CVBuilderService cvBuilderService,
            CVDraftService cvDraftService,
            CVAiImprovementService cvAiImprovementService,
            CVAiAssistantService cvAiAssistantService,
            CVAiJobMatchService cvAiJobMatchService) {
        this.currentUserService = currentUserService;
        this.cvProfileService = cvProfileService;
        this.cvBuilderService = cvBuilderService;
        this.cvDraftService = cvDraftService;
        this.cvAiImprovementService = cvAiImprovementService;
        this.cvAiAssistantService = cvAiAssistantService;
        this.cvAiJobMatchService = cvAiJobMatchService;
    }

    @GetMapping("/profile")
    public CVProfileResponse getMyProfile() {
        return cvProfileService.getForUser(currentUserService.getCurrentUser());
    }

    @PutMapping("/profile")
    public CVProfileResponse updateMyProfile(@Valid @RequestBody UpdateCVProfileRequest request) {
        return cvProfileService.updateForUser(currentUserService.getCurrentUser(), request);
    }

    @GetMapping("/preview")
    public CVPreviewResponse getMyPreview(
            @RequestParam(required = false) String template,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer projectLimit) {
        return cvBuilderService.buildForUser(
                currentUserService.getCurrentUser(),
                CVPreviewOptions.builder()
                        .template(template)
                        .language(language)
                        .projectLimit(projectLimit)
                        .build()
        );
    }

    @PostMapping("/drafts/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public CVDraftResponse generateMyDraft() {
        return cvDraftService.generateForUser(currentUserService.getCurrentUser());
    }

    @GetMapping("/drafts/latest")
    public CVDraftResponse getLatestDraft() {
        return cvDraftService.getLatestForUser(currentUserService.getCurrentUser());
    }

    @PutMapping("/drafts/{draftId}")
    public CVDraftResponse updateMyDraft(
            @PathVariable Long draftId,
            @Valid @RequestBody UpdateCVDraftRequest request) {
        return cvDraftService.updateForUser(currentUserService.getCurrentUser(), draftId, request);
    }

    @PostMapping("/ai/improve")
    public CvAiImproveResponse improveMyCvText(@Valid @RequestBody CvAiImproveRequest request) {
        return cvAiImprovementService.improveForUser(currentUserService.getCurrentUser(), request);
    }

    @PostMapping("/ai/chat")
    public CvAiChatResponse chatWithMyCvAssistant(@Valid @RequestBody CvAiChatRequest request) {
        return cvAiAssistantService.chatForUser(currentUserService.getCurrentUser(), request);
    }

    @PostMapping("/ai/job-match")
    public CvAiJobMatchResponse generateJobMatchedCv(@Valid @RequestBody CvAiJobMatchRequest request) {
        return cvAiJobMatchService.matchForUser(currentUserService.getCurrentUser(), request);
    }
}
