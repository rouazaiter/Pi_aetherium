package com.education.platform.controllers.portfolio;

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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio-ai/me")
public class PortfolioAiController {

    private final PortfolioDnaSummaryService portfolioDnaSummaryService;
    private final StrengthsGapsService strengthsGapsService;
    private final NextBestMovesService nextBestMovesService;
    private final PortfolioCoherenceService portfolioCoherenceService;
    private final MentorChatService mentorChatService;

    public PortfolioAiController(
            PortfolioDnaSummaryService portfolioDnaSummaryService,
            StrengthsGapsService strengthsGapsService,
            NextBestMovesService nextBestMovesService,
            PortfolioCoherenceService portfolioCoherenceService,
            MentorChatService mentorChatService) {
        this.portfolioDnaSummaryService = portfolioDnaSummaryService;
        this.strengthsGapsService = strengthsGapsService;
        this.nextBestMovesService = nextBestMovesService;
        this.portfolioCoherenceService = portfolioCoherenceService;
        this.mentorChatService = mentorChatService;
    }

    @GetMapping("/profile-strength")
    public PortfolioDnaSummaryDto getProfileStrength() {
        return portfolioDnaSummaryService.analyzeCurrentUser();
    }

    @GetMapping("/strengths-gaps")
    public StrengthsGapsDto getStrengthsGaps() {
        return strengthsGapsService.analyzeCurrentUser();
    }

    @GetMapping("/next-best-moves")
    public NextBestMovesDto getNextBestMoves() {
        return nextBestMovesService.analyzeCurrentUser();
    }

    @GetMapping("/coherence")
    public ProfileCoherenceDto getCoherence() {
        return portfolioCoherenceService.analyzeCurrentUser();
    }

    @PostMapping("/mentor-chat")
    public MentorChatResponseDto mentorChat(@Valid @RequestBody MentorChatRequestDto request) {
        return mentorChatService.chat(request);
    }
}
