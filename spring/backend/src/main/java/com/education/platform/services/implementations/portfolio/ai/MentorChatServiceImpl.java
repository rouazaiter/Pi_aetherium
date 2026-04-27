package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.common.ApiException;
import com.education.platform.dto.portfolio.ai.MentorChatRequestDto;
import com.education.platform.dto.portfolio.ai.MentorChatResponseDto;
import com.education.platform.entities.User;
import com.education.platform.entities.admin.jihenportfolio.AiFeature;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminTrackingService;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.cv.OllamaClient;
import com.education.platform.services.interfaces.portfolio.ai.MentorChatService;
import com.education.platform.services.interfaces.portfolio.ai.NextBestMovesService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioCoherenceService;
import com.education.platform.services.interfaces.portfolio.ai.PortfolioDnaSummaryService;
import com.education.platform.services.interfaces.portfolio.ai.StrengthsGapsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MentorChatServiceImpl extends AbstractPortfolioAiService implements MentorChatService {

    private final CurrentUserService currentUserService;
    private final PortfolioDnaSummaryService portfolioDnaSummaryService;
    private final StrengthsGapsService strengthsGapsService;
    private final NextBestMovesService nextBestMovesService;
    private final PortfolioCoherenceService portfolioCoherenceService;
    private final OllamaClient ollamaClient;
    private final JihenPortfolioAdminTrackingService trackingService;

    public MentorChatServiceImpl(
            CurrentUserService currentUserService,
            PortfolioRepository portfolioRepository,
            PortfolioDnaSummaryService portfolioDnaSummaryService,
            StrengthsGapsService strengthsGapsService,
            NextBestMovesService nextBestMovesService,
            PortfolioCoherenceService portfolioCoherenceService,
            OllamaClient ollamaClient,
            JihenPortfolioAdminTrackingService trackingService) {
        super(currentUserService, portfolioRepository);
        this.currentUserService = currentUserService;
        this.portfolioDnaSummaryService = portfolioDnaSummaryService;
        this.strengthsGapsService = strengthsGapsService;
        this.nextBestMovesService = nextBestMovesService;
        this.portfolioCoherenceService = portfolioCoherenceService;
        this.ollamaClient = ollamaClient;
        this.trackingService = trackingService;
    }

    @Override
    @Transactional(readOnly = true)
    public MentorChatResponseDto chat(MentorChatRequestDto request) {
        if (request == null || !PortfolioAiSupport.hasText(request.getMessage())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message is required");
        }

        User currentUser = currentUserService.getCurrentUser();
        String userName = resolveDisplayName(currentUser);
        Portfolio portfolio = getCurrentUserPortfolio();
        var dna = portfolioDnaSummaryService.analyzeCurrentUser();
        var strengthsGaps = strengthsGapsService.analyzeCurrentUser();
        var nextMoves = nextBestMovesService.analyzeCurrentUser();
        var coherence = portfolioCoherenceService.analyzeCurrentUser();
        long startedAt = System.nanoTime();

        String prompt = buildPrompt(userName, portfolio, dna, strengthsGaps, nextMoves, coherence, request);

        try {
            String raw = ollamaClient.generate(prompt);
            trackingService.recordAiUsage(AiFeature.PORTFOLIO_MENTOR, currentUser.getId(), true, elapsedMillis(startedAt), null);
            return parseResponse(raw);
        } catch (Exception e) {
            trackingService.recordAiUsage(AiFeature.PORTFOLIO_MENTOR, currentUser.getId(), false, elapsedMillis(startedAt), e.getMessage());
            return fallbackResponse(userName, dna, strengthsGaps, nextMoves, coherence);
        }
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }

    private String buildPrompt(String userName, Portfolio portfolio, com.education.platform.dto.portfolio.ai.PortfolioDnaSummaryDto dna,
                               com.education.platform.dto.portfolio.ai.StrengthsGapsDto strengthsGaps,
                               com.education.platform.dto.portfolio.ai.NextBestMovesDto nextMoves,
                               com.education.platform.dto.portfolio.ai.ProfileCoherenceDto coherence,
                               MentorChatRequestDto request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a portfolio mentor.\n");
        prompt.append("You are helping a user named ").append(userName).append(".\n");
        prompt.append("Use only the real portfolio facts below.\n");
        prompt.append("Do not invent skills, projects, metrics, experience, companies, or outcomes.\n");
        prompt.append("Be friendly, practical, and direct.\n");
        prompt.append("Give practical advice, not generic advice.\n");
        prompt.append("If rewriting is useful, keep it grounded in the actual data.\n");
        prompt.append("Start the response with: \"Hi ").append(userName).append(",\"\n");
        prompt.append("Use the name only once or naturally; do not repeat it excessively.\n");
        prompt.append("Reply mode: ").append(PortfolioAiSupport.hasText(request.getReplyMode()) ? request.getReplyMode().trim() : inferReplyMode(request.getMessage())).append("\n");
        prompt.append("Target: ").append(PortfolioAiSupport.hasText(request.getTarget()) ? request.getTarget().trim() : inferTarget(request.getMessage())).append("\n");
        prompt.append("User question: ").append(request.getMessage().trim()).append("\n\n");

        prompt.append("PORTFOLIO:\n");
        prompt.append("Title: ").append(safe(portfolio.getTitle())).append("\n");
        prompt.append("Job: ").append(safe(portfolio.getJob())).append("\n");
        prompt.append("Bio: ").append(safe(portfolio.getBio())).append("\n");
        prompt.append("Skills: ").append(String.join(", ", PortfolioAiSupport.topSkillNames(portfolio, 20))).append("\n\n");

        prompt.append("PROJECTS:\n");
        List<PortfolioProject> projects = PortfolioAiSupport.sortedProjects(portfolio);
        if (projects.isEmpty()) {
            prompt.append("No project facts available.\n");
        } else {
            for (PortfolioProject project : projects.stream().limit(6).toList()) {
                prompt.append("- Title: ").append(safe(project.getTitle())).append("\n");
                prompt.append("  Description: ").append(safe(project.getDescription())).append("\n");
                prompt.append("  URL: ").append(safe(project.getProjectUrl())).append("\n");
                prompt.append("  Pinned: ").append(Boolean.TRUE.equals(project.getPinned()) ? "YES" : "NO").append("\n");
                prompt.append("  Skills: ").append(String.join(", ", PortfolioAiSupport.skillNames(project.getSkills()))).append("\n");
            }
        }

        prompt.append("\nANALYSIS:\n");
        prompt.append("Dominant family: ").append(dna.getDominantFamily()).append("\n");
        prompt.append("DNA type: ").append(safe(dna.getDnaType())).append("\n");
        prompt.append("Profile strength score: ").append(dna.getProfileStrengthScore()).append("/100\n");
        prompt.append("Market readiness: ").append(safe(dna.getMarketReadiness())).append("\n");
        prompt.append("Strongest signals: ").append(String.join(", ", dna.getStrongestSignals())).append("\n");
        prompt.append("Main weak points: ").append(String.join(", ", dna.getMainWeakPoints())).append("\n");
        prompt.append("Coherence status: ").append(safe(coherence.getStatus())).append("\n");
        prompt.append("Mismatch detected: ").append(Boolean.TRUE.equals(coherence.getMismatchDetected()) ? "YES" : "NO").append("\n");
        prompt.append("Strengths: ").append(strengthsGaps.getStrengths() == null ? "" : strengthsGaps.getStrengths().stream().map(item -> item.getLabel()).reduce((a, b) -> a + ", " + b).orElse("")).append("\n");
        prompt.append("Gaps: ").append(strengthsGaps.getGaps() == null ? "" : strengthsGaps.getGaps().stream().map(item -> item.getLabel()).reduce((a, b) -> a + ", " + b).orElse("")).append("\n");
        prompt.append("Next best moves: ").append(nextMoves.getCoreMoves() == null ? "" : nextMoves.getCoreMoves().stream().map(item -> item.getLabel()).reduce((a, b) -> a + ", " + b).orElse("")).append("\n\n");

        prompt.append("OUTPUT FORMAT:\n");
        prompt.append("MAIN_MESSAGE: one short direct paragraph\n");
        prompt.append("NOTE_1: one practical note\n");
        prompt.append("NOTE_2: one practical note\n");
        prompt.append("NOTE_3: optional practical note\n");
        prompt.append("REWRITE_1: optional rewrite\n");
        prompt.append("REWRITE_2: optional rewrite\n");
        prompt.append("REWRITE_3: optional rewrite\n");
        return prompt.toString();
    }

    private MentorChatResponseDto parseResponse(String raw) {
        String sanitized = PortfolioAiTextSanitizer.sanitize(raw);
        String mainMessage = "";
        List<String> notes = new ArrayList<>();
        List<String> rewrites = new ArrayList<>();

        for (String line : sanitized.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("MAIN_MESSAGE:")) {
                mainMessage = trimmed.replaceFirst("MAIN_MESSAGE:\\s*", "").trim();
            } else if (trimmed.startsWith("NOTE_1:") || trimmed.startsWith("NOTE_2:") || trimmed.startsWith("NOTE_3:")) {
                String note = trimmed.replaceFirst("NOTE_\\d+:\\s*", "").trim();
                if (!note.isBlank()) notes.add(note);
            } else if (trimmed.startsWith("REWRITE_1:") || trimmed.startsWith("REWRITE_2:") || trimmed.startsWith("REWRITE_3:")) {
                String rewrite = trimmed.replaceFirst("REWRITE_\\d+:\\s*", "").trim();
                if (!rewrite.isBlank()) rewrites.add(rewrite);
            }
        }

        if (mainMessage.isBlank()) {
            mainMessage = sanitized.lines().findFirst().orElse("").trim();
        }

        return MentorChatResponseDto.builder()
                .mainMessage(mainMessage)
                .notes(notes)
                .rewrites(rewrites)
                .rawResponse(sanitized)
                .build();
    }

    private MentorChatResponseDto fallbackResponse(
            String userName,
            com.education.platform.dto.portfolio.ai.PortfolioDnaSummaryDto dna,
            com.education.platform.dto.portfolio.ai.StrengthsGapsDto strengthsGaps,
            com.education.platform.dto.portfolio.ai.NextBestMovesDto nextMoves,
            com.education.platform.dto.portfolio.ai.ProfileCoherenceDto coherence) {
        List<String> notes = new ArrayList<>();
        if (nextMoves.getCoreMoves() != null) {
            notes.addAll(nextMoves.getCoreMoves().stream().limit(3).map(item -> item.getLabel()).toList());
        }
        if (notes.isEmpty()) {
            notes = List.of(
                    "Focus first on the highest-priority gap in your main direction.",
                    "Strengthen the projects that already best support your strongest skills.",
                    "Keep the title, bio, and project proof aligned."
            );
        }
        return MentorChatResponseDto.builder()
                .mainMessage("Hi " + userName + ", your portfolio currently looks strongest in " + PortfolioAiSupport.formatFamilyLabel(dna.getDominantFamily()).toLowerCase()
                        + ". The main weak points are " + String.join(", ", dna.getMainWeakPoints()) + ".")
                .notes(notes)
                .rewrites(List.of())
                .rawResponse("Fallback response used because the local AI was unavailable.")
                .build();
    }

    private String inferTarget(String message) {
        String lower = message == null ? "" : message.toLowerCase();
        if (lower.contains("bio")) return "bio";
        if (lower.contains("title")) return "title";
        if (lower.contains("project")) return lower.contains("projects") ? "projects" : "project";
        if (lower.contains("skill")) return "skills";
        return "general";
    }

    private String inferReplyMode(String message) {
        String lower = message == null ? "" : message.toLowerCase();
        if (lower.contains("rewrite") || lower.contains("improve") || lower.contains("make") && lower.contains("stronger")) return "REWRITE";
        if (lower.contains("why") || lower.contains("explain")) return "EXPLAIN";
        if (lower.contains("compare")) return "COMPARE";
        return "ADVICE";
    }

    private String safe(String value) {
        return PortfolioAiSupport.hasText(value) ? value.trim() : "Not provided";
    }

    private String resolveDisplayName(User user) {
        if (user != null && PortfolioAiSupport.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        if (user != null && PortfolioAiSupport.hasText(user.getEmail())) {
            String email = user.getEmail().trim();
            int atIndex = email.indexOf('@');
            String candidate = atIndex > 0 ? email.substring(0, atIndex).trim() : email;
            if (!candidate.isBlank()) {
                return candidate;
            }
        }
        return "there";
    }
}
