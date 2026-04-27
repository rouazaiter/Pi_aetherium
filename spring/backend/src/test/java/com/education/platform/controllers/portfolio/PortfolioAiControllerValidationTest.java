package com.education.platform.controllers.portfolio;

import com.education.platform.common.GlobalExceptionHandler;
import com.education.platform.services.interfaces.portfolio.ai.MentorChatService;
import com.education.platform.services.interfaces.portfolio.ai.NextBestMovesService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioCoherenceService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioDnaSummaryService;
import com.education.platform.services.interfaces.portfolio.ai.StrengthsGapsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortfolioAiControllerValidationTest {

    @Test
    void mentorChatReturnsBadRequestWhenMessageIsBlank() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PortfolioAiController(
                        mock(PortfolioDnaSummaryService.class),
                        mock(StrengthsGapsService.class),
                        mock(NextBestMovesService.class),
                        mock(PortfolioCoherenceService.class),
                        mock(MentorChatService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/portfolio-ai/me/mentor-chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "   ",
                                  "target": "general",
                                  "replyMode": "ADVICE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists());
    }
}
