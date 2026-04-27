package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.dto.cv.CVProfileResponse;
import com.education.platform.dto.cv.CvAiChatContextMode;
import com.education.platform.dto.cv.CvAiChatRequest;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVSection;
import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.cv.CVProfileService;
import com.education.platform.services.interfaces.cv.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CVAiAssistantServiceImplTest {

    @Mock
    private CVDraftRepository cvDraftRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioProjectRepository portfolioProjectRepository;

    @Mock
    private CVProfileService cvProfileService;

    @Mock
    private OllamaClient ollamaClient;

    @Test
    void chatForUserUsesLatestDraftAndReturnsCleanReply() {
        CVAiAssistantServiceImpl service = new CVAiAssistantServiceImpl(
                cvDraftRepository,
                portfolioRepository,
                portfolioProjectRepository,
                cvProfileService,
                ollamaClient,
                new ObjectMapper()
        );

        User user = User.builder()
                .id(1L)
                .username("jihen")
                .email("jihen@example.com")
                .profile(Profile.builder().firstName("Jihen").lastName("Doe").description("Developer").build())
                .build();

        CVDraft draft = CVDraft.builder()
                .id(11L)
                .user(user)
                .theme("ATS_MINIMAL")
                .build();
        draft.addSection(CVSection.builder()
                .id(21L)
                .type(CVSectionType.PROFILE)
                .title("Profile")
                .orderIndex(0)
                .visible(true)
                .contentJson("{\"summary\":\"Java developer building APIs\"}")
                .build());

        Skill java = Skill.builder().id(1L).name("Java").build();
        Portfolio portfolio = Portfolio.builder()
                .id(31L)
                .job("Backend Developer")
                .bio("Backend developer focused on APIs.")
                .skills(Set.of(java))
                .build();
        PortfolioProject project = PortfolioProject.builder()
                .id(41L)
                .title("Education API")
                .description("Built backend APIs for an education platform using Java.")
                .skills(Set.of(java))
                .build();

        CvAiChatRequest request = new CvAiChatRequest();
        request.setMessage("Improve my CV");
        request.setContextMode(CvAiChatContextMode.CURRENT_CV);

        CVProfileResponse cvProfile = CVProfileResponse.builder()
                .headline("Backend Developer")
                .summary("Backend developer focused on APIs.")
                .build();

        when(cvDraftRepository.findTopByUser_IdOrderByUpdatedAtDescIdDesc(1L)).thenReturn(Optional.of(draft));
        when(portfolioRepository.findByUser_Id(1L)).thenReturn(Optional.of(portfolio));
        when(portfolioProjectRepository.findByPortfolio_IdOrderByCreatedAtDesc(31L)).thenReturn(List.of(project));
        when(cvProfileService.getForUser(user)).thenReturn(cvProfile);
        when(ollamaClient.generate(anyString())).thenReturn("""
                <think>hidden</think>
                ## CV Review
                CV Score: 78/100
                - Main issues: summary is too short.
                - Concrete improvements: make project outcomes clearer.
                - Next steps: strengthen summary.
                """);

        var response = service.chatForUser(user, request);

        assertEquals(48, response.getScore());
        assertEquals(true, response.getReply().contains("CV Score: 78/100"));
        assertEquals(0, response.getSuggestedActions().size());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(ollamaClient).generate(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertEquals(true, prompt.contains("Current CV score: 48/100."));
        assertEquals(true, prompt.contains("\"draftId\":11"));
        assertEquals(true, prompt.contains("\"title\":\"Education API\""));
    }

    @Test
    void chatForUserThrowsWhenDraftIsMissing() {
        CVAiAssistantServiceImpl service = new CVAiAssistantServiceImpl(
                cvDraftRepository,
                portfolioRepository,
                portfolioProjectRepository,
                cvProfileService,
                ollamaClient,
                new ObjectMapper()
        );

        User user = User.builder().id(2L).build();
        CvAiChatRequest request = new CvAiChatRequest();
        request.setMessage("Score my CV");
        request.setContextMode(CvAiChatContextMode.CURRENT_CV);

        when(cvDraftRepository.findTopByUser_IdOrderByUpdatedAtDescIdDesc(2L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> service.chatForUser(user, request));

        assertEquals(404, exception.getStatus().value());
        assertEquals("CV draft not found", exception.getMessage());
    }
}
