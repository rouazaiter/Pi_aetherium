package com.education.platform.controllers.cv;

import com.education.platform.dto.cv.CVDraftResponse;
import com.education.platform.dto.cv.CvAiChatContextMode;
import com.education.platform.dto.cv.CvAiChatRequest;
import com.education.platform.dto.cv.CvAiChatResponse;
import com.education.platform.dto.cv.CvAiImproveMaxLength;
import com.education.platform.dto.cv.CvAiImproveRequest;
import com.education.platform.dto.cv.CvAiImproveResponse;
import com.education.platform.dto.cv.CvAiImproveSectionType;
import com.education.platform.dto.cv.CvAiImproveTargetTone;
import com.education.platform.dto.cv.CvAiImproveTopic;
import com.education.platform.dto.cv.CVPreviewOptions;
import com.education.platform.dto.cv.CVPreviewResponse;
import com.education.platform.dto.cv.CVProfileResponse;
import com.education.platform.dto.cv.UpdateCVDraftRequest;
import com.education.platform.dto.cv.UpdateCVProfileRequest;
import com.education.platform.entities.User;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.cv.CVAiAssistantService;
import com.education.platform.services.interfaces.cv.CVAiImprovementService;
import com.education.platform.services.interfaces.cv.CVBuilderService;
import com.education.platform.services.interfaces.cv.CVDraftService;
import com.education.platform.services.interfaces.cv.CVProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CVControllerTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CVProfileService cvProfileService;

    @Mock
    private CVBuilderService cvBuilderService;

    @Mock
    private CVDraftService cvDraftService;

    @Mock
    private CVAiImprovementService cvAiImprovementService;

    @Mock
    private CVAiAssistantService cvAiAssistantService;

    @InjectMocks
    private CVController cvController;

    @Test
    void updateMyProfileDelegatesToProfileServiceForCurrentUser() {
        User user = User.builder().id(1L).build();
        UpdateCVProfileRequest request = new UpdateCVProfileRequest();
        request.setHeadline("Platform Engineer");
        CVProfileResponse response = CVProfileResponse.builder()
                .headline("Platform Engineer")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cvProfileService.updateForUser(user, request)).thenReturn(response);

        CVProfileResponse result = cvController.updateMyProfile(request);

        assertEquals("Platform Engineer", result.getHeadline());
        verify(cvProfileService).updateForUser(user, request);
    }

    @Test
    void getMyPreviewPassesOptionalQueryParametersToBuilderService() {
        User user = User.builder().id(2L).build();
        CVPreviewResponse response = CVPreviewResponse.builder().build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cvBuilderService.buildForUser(org.mockito.ArgumentMatchers.eq(user), org.mockito.ArgumentMatchers.any(CVPreviewOptions.class)))
                .thenReturn(response);

        CVPreviewResponse result = cvController.getMyPreview("academic", "fr", 5);

        assertEquals(response, result);

        ArgumentCaptor<CVPreviewOptions> captor = ArgumentCaptor.forClass(CVPreviewOptions.class);
        verify(cvBuilderService).buildForUser(org.mockito.ArgumentMatchers.eq(user), captor.capture());
        assertEquals("academic", captor.getValue().getTemplate());
        assertEquals("fr", captor.getValue().getLanguage());
        assertEquals(5, captor.getValue().getProjectLimit());
    }

    @Test
    void generateMyDraftDelegatesToDraftService() {
        User user = User.builder().id(3L).build();
        CVDraftResponse response = CVDraftResponse.builder()
                .id(10L)
                .theme("ATS_MINIMAL")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cvDraftService.generateForUser(user)).thenReturn(response);

        CVDraftResponse result = cvController.generateMyDraft();

        assertEquals(10L, result.getId());
        verify(cvDraftService).generateForUser(user);
    }

    @Test
    void getLatestDraftDelegatesToDraftService() {
        User user = User.builder().id(4L).build();
        CVDraftResponse response = CVDraftResponse.builder()
                .id(12L)
                .theme("ATS_MINIMAL")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cvDraftService.getLatestForUser(user)).thenReturn(response);

        CVDraftResponse result = cvController.getLatestDraft();

        assertEquals(12L, result.getId());
        verify(cvDraftService).getLatestForUser(user);
    }

    @Test
    void updateMyDraftDelegatesToDraftService() {
        User user = User.builder().id(5L).build();
        UpdateCVDraftRequest request = new UpdateCVDraftRequest();
        request.setTheme("modern");
        CVDraftResponse response = CVDraftResponse.builder()
                .id(15L)
                .theme("ATS_MINIMAL")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cvDraftService.updateForUser(user, 15L, request)).thenReturn(response);

        CVDraftResponse result = cvController.updateMyDraft(15L, request);

        assertEquals(15L, result.getId());
        verify(cvDraftService).updateForUser(user, 15L, request);
    }

    @Test
    void improveMyCvTextDelegatesToAiService() {
        User user = User.builder().id(6L).build();
        CvAiImproveRequest request = new CvAiImproveRequest();
        request.setTopic(CvAiImproveTopic.PROFILE_SUMMARY);
        request.setSectionType(CvAiImproveSectionType.PROFILE);
        request.setText("backend developer with java skills");
        request.setTargetTone(CvAiImproveTargetTone.ATS_PROFESSIONAL);
        request.setMaxLength(CvAiImproveMaxLength.SHORT);
        CvAiImproveResponse response = new CvAiImproveResponse("Backend developer with strong Java skills.");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cvAiImprovementService.improveForUser(user, request)).thenReturn(response);

        CvAiImproveResponse result = cvController.improveMyCvText(request);

        assertEquals("Backend developer with strong Java skills.", result.getSuggestion());
        verify(cvAiImprovementService).improveForUser(user, request);
    }

    @Test
    void chatWithMyCvAssistantDelegatesToAssistantService() {
        User user = User.builder().id(7L).build();
        CvAiChatRequest request = new CvAiChatRequest();
        request.setMessage("Improve my CV");
        request.setContextMode(CvAiChatContextMode.CURRENT_CV);
        CvAiChatResponse response = CvAiChatResponse.builder()
                .reply("CV Score: 78/100\nMain issues:\nSummary is too short.")
                .score(78)
                .suggestedActions(java.util.List.of())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cvAiAssistantService.chatForUser(user, request)).thenReturn(response);

        CvAiChatResponse result = cvController.chatWithMyCvAssistant(request);

        assertEquals(78, result.getScore());
        verify(cvAiAssistantService).chatForUser(user, request);
    }
}
