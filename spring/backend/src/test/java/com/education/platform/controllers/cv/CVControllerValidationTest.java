package com.education.platform.controllers.cv;

import com.education.platform.common.GlobalExceptionHandler;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.cv.CVAiAssistantService;
import com.education.platform.services.interfaces.cv.CVAiImprovementService;
import com.education.platform.services.interfaces.cv.CVBuilderService;
import com.education.platform.services.interfaces.cv.CVDraftService;
import com.education.platform.services.interfaces.cv.CVProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

class CVControllerValidationTest {

    @Test
    void improveMyCvTextReturnsBadRequestWhenTextIsBlank() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CVController(
                        mock(CurrentUserService.class),
                        mock(CVProfileService.class),
                        mock(CVBuilderService.class),
                        mock(CVDraftService.class),
                        mock(CVAiImprovementService.class),
                        mock(CVAiAssistantService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/cv/me/ai/improve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "topic": "PROFILE_SUMMARY",
                                  "sectionType": "PROFILE",
                                  "field": "summary",
                                  "text": "   ",
                                  "targetTone": "ATS_PROFESSIONAL",
                                  "maxLength": "SHORT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.text").exists());
    }

    @Test
    void chatWithMyCvAssistantReturnsBadRequestWhenMessageIsBlank() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CVController(
                        mock(CurrentUserService.class),
                        mock(CVProfileService.class),
                        mock(CVBuilderService.class),
                        mock(CVDraftService.class),
                        mock(CVAiImprovementService.class),
                        mock(CVAiAssistantService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/cv/me/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "   ",
                                  "draftId": null,
                                  "contextMode": "CURRENT_CV"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists());
    }
}
