package com.education.platform.controllers.portfolio;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.dto.portfolio.ai.MentorChatRequestDto;
import com.education.platform.dto.portfolio.ai.MentorChatResponseDto;
import com.education.platform.dto.portfolio.ai.NextBestMovesDto;
import com.education.platform.dto.portfolio.ai.PortfolioDnaSummaryDto;
import com.education.platform.dto.portfolio.ai.ProfileCoherenceDto;
import com.education.platform.dto.portfolio.ai.StrengthsGapsDto;
import com.education.platform.services.interfaces.portfolio.ai.MentorChatService;
import com.education.platform.services.interfaces.portfolio.ai.NextBestMovesService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioCoherenceService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioDnaSummaryService;
import com.education.platform.services.interfaces.portfolio.ai.StrengthsGapsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioAiControllerTest {

    @Mock
    private PortfolioDnaSummaryService portfolioDnaSummaryService;
    @Mock
    private StrengthsGapsService strengthsGapsService;
    @Mock
    private NextBestMovesService nextBestMovesService;
    @Mock
    private PortfolioCoherenceService portfolioCoherenceService;
    @Mock
    private MentorChatService mentorChatService;

    @InjectMocks
    private PortfolioAiController controller;

    @Test
    void getProfileStrengthDelegatesToService() {
        PortfolioDnaSummaryDto response = PortfolioDnaSummaryDto.builder()
                .dominantFamily(DeveloperFamily.BACKEND)
                .profileStrengthScore(82)
                .build();
        when(portfolioDnaSummaryService.analyzeCurrentUser()).thenReturn(response);

        PortfolioDnaSummaryDto result = controller.getProfileStrength();

        assertEquals(82, result.getProfileStrengthScore());
        verify(portfolioDnaSummaryService).analyzeCurrentUser();
    }

    @Test
    void mentorChatDelegatesToService() {
        MentorChatRequestDto request = MentorChatRequestDto.builder().message("Improve my portfolio").build();
        MentorChatResponseDto response = MentorChatResponseDto.builder()
                .mainMessage("Focus on project proof.")
                .notes(List.of("Add testing proof."))
                .build();
        when(mentorChatService.chat(request)).thenReturn(response);

        MentorChatResponseDto result = controller.mentorChat(request);

        assertEquals("Focus on project proof.", result.getMainMessage());
        verify(mentorChatService).chat(request);
    }

    @Test
    void getCoherenceDelegatesToService() {
        ProfileCoherenceDto response = ProfileCoherenceDto.builder().totalScore(68).status("MEDIUM").build();
        when(portfolioCoherenceService.analyzeCurrentUser()).thenReturn(response);

        ProfileCoherenceDto result = controller.getCoherence();

        assertEquals(68, result.getTotalScore());
        verify(portfolioCoherenceService).analyzeCurrentUser();
    }

    @Test
    void getStrengthsGapsDelegatesToService() {
        StrengthsGapsDto response = StrengthsGapsDto.builder().dominantFamily(DeveloperFamily.BACKEND).summary("Summary").build();
        when(strengthsGapsService.analyzeCurrentUser()).thenReturn(response);

        StrengthsGapsDto result = controller.getStrengthsGaps();

        assertEquals(DeveloperFamily.BACKEND, result.getDominantFamily());
        verify(strengthsGapsService).analyzeCurrentUser();
    }

    @Test
    void getNextBestMovesDelegatesToService() {
        NextBestMovesDto response = NextBestMovesDto.builder().dominantFamily(DeveloperFamily.BACKEND).summary("Next").build();
        when(nextBestMovesService.analyzeCurrentUser()).thenReturn(response);

        NextBestMovesDto result = controller.getNextBestMoves();

        assertEquals(DeveloperFamily.BACKEND, result.getDominantFamily());
        verify(nextBestMovesService).analyzeCurrentUser();
    }
}
