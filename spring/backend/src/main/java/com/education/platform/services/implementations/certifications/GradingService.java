package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.education.platform.dto.certifications.GradeRequest;
import com.education.platform.dto.certifications.GradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GradingService {

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openAiUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Uses the LLM to grade an open-ended answer.
     * Returns a GradeResponse with correct/score/feedback.
     */
    public GradeResponse grade(GradeRequest req) {
        // Empty answer — fail immediately without calling LLM
        if (req.userAnswer() == null || req.userAnswer().isBlank()) {
            return new GradeResponse(false, 0,
                    "No answer provided.", req.expectedAnswer());
        }

        String prompt = buildGradingPrompt(req);
        String raw = callLlm(prompt);
        return parseGradeResponse(raw, req.expectedAnswer());
    }

    // ─── PROMPT ──────────────────────────────────────────────────────────────

    private String buildGradingPrompt(GradeRequest req) {
        String typeContext = switch (req.questionType()) {
            case "EXPLAIN" -> "This is an explanation question. The student must demonstrate understanding of the concept.";
            case "WRITE"   -> "This is a written response question. Evaluate completeness and accuracy.";
            case "CODE"    -> "This is a free-text code question" +
                    (req.codeLanguage() != null ? " in " + req.codeLanguage() : "") +
                    ". Evaluate correctness of logic and syntax.";
            default        -> "Evaluate the answer for correctness and completeness.";
        };

        return """
                You are a strict but fair exam grader. Grade the following student answer.

                Question: %s
                Question Type: %s
                %s

                Model Answer (rubric): %s

                Student Answer: %s

                Grade this answer and return ONLY valid JSON (no markdown):
                {
                  "correct": true or false,
                  "score": 0-100,
                  "feedback": "One or two sentences explaining why the answer is correct or what is missing/wrong."
                }

                Grading rules:
                - "correct" = true only if score >= 70
                - Be strict: partial knowledge should score 40-69 (not correct)
                - A completely wrong or irrelevant answer scores 0-20
                - A mostly correct answer with minor gaps scores 70-85
                - A complete, accurate answer scores 86-100
                - If the student wrote something completely unrelated (e.g. a random word), score = 0
                - Keep feedback concise, educational, and constructive
                """.formatted(
                req.questionText(),
                req.questionType(),
                typeContext,
                req.expectedAnswer() != null ? req.expectedAnswer() : "(no model answer provided — use your knowledge)",
                req.userAnswer()
        );
    }

    // ─── LLM CALL ────────────────────────────────────────────────────────────

    private String callLlm(String prompt) {
        if (openAiKey == null || openAiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);
        headers.set("HTTP-Referer", "http://localhost:4200");
        headers.set("X-Title", "SkillHub Grader");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.2);   // low temp for consistent grading
        body.put("max_tokens", 300);    // grading response is short
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || choices.isEmpty()) {
                throw new RuntimeException("LLM returned no choices");
            }
            return choices.get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call grading LLM: " + e.getMessage());
        }
    }

    // ─── PARSE RESPONSE ───────────────────────────────────────────────────────

    private GradeResponse parseGradeResponse(String raw, String modelAnswer) {
        try {
            String cleaned = raw.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-zA-Z]*\\n?", "").replace("```", "").trim();
            }
            JsonNode root = objectMapper.readTree(cleaned);
            boolean correct  = root.path("correct").asBoolean(false);
            int     score    = root.path("score").asInt(0);
            String  feedback = root.path("feedback").asText("No feedback available.");
            return new GradeResponse(correct, score, feedback, modelAnswer);
        } catch (Exception e) {
            // Fallback: if parsing fails, mark as incorrect
            return new GradeResponse(false, 0,
                    "Could not evaluate answer automatically. Please review manually.", modelAnswer);
        }
    }
}
