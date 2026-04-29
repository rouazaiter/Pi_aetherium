package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.education.platform.entities.certifications.Certification;
import com.education.platform.entities.certifications.Question;
import com.education.platform.repositories.certifications.CertificationRepository;
import com.education.platform.repositories.certifications.ExamRepository;
import com.education.platform.repositories.certifications.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a fresh set of practice questions via LLM.
 * These are completely different from the real exam questions —
 * same topic and difficulty, but new question texts and options.
 */
@Service
@RequiredArgsConstructor
public class PracticeQuestionService {

    private final CertificationRepository certRepo;
    private final ExamRepository examRepo;
    private final QuestionRepository questionRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openAiUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    // ── Public entry point ────────────────────────────────────────────────────

    public List<Map<String, Object>> generatePracticeQuestions(Long certId) {
        return generatePracticeQuestions(certId, 10);
    }

    public List<Map<String, Object>> generatePracticeQuestions(Long certId, int requestedCount) {
        Certification cert = certRepo.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        // Collect existing real exam question texts so LLM avoids them
        List<String> existingTexts = examRepo.findByCertificationId(certId).stream()
                .flatMap(exam -> questionRepo.findByExamIdOrderByOrderIndexAsc(exam.getId()).stream())
                .map(q -> q.getQuestionText().substring(0, Math.min(80, q.getQuestionText().length())))
                .collect(Collectors.toList());

        String topic      = cert.getTitle();
        String category   = cert.getCategory() != null ? cert.getCategory() : "General";
        String difficulty = cert.getDifficulty() != null ? cert.getDifficulty().name() : "INTERMEDIATE";

        // Cap each LLM call at 20 questions to stay within token limits
        final int BATCH_SIZE = 20;
        int count = Math.max(1, Math.min(requestedCount, 75)); // clamp 1-75

        List<Map<String, Object>> allQuestions = new ArrayList<>();
        int offset = 0;

        while (allQuestions.size() < count) {
            int batchCount = Math.min(BATCH_SIZE, count - allQuestions.size());
            String prompt = buildPrompt(topic, category, difficulty, batchCount, existingTexts, offset);
            String raw    = callLlm(prompt);
            List<Map<String, Object>> batch = parseQuestions(raw, offset);
            if (batch.isEmpty()) break; // LLM returned nothing — stop
            allQuestions.addAll(batch);
            offset = allQuestions.size();
        }

        return allQuestions;
    }

    // ── Prompt ────────────────────────────────────────────────────────────────

    private String buildPrompt(String topic, String category, String difficulty,
                                int count, List<String> existingTexts, int offset) {
        String avoidList = existingTexts.isEmpty() ? "none"
                : existingTexts.stream().limit(15)
                    .map(t -> "- " + t)
                    .collect(Collectors.joining("\n"));

        return """
                You are generating a WARM-UP exam for a certification platform.
                
                Topic: %s | Category: %s | Difficulty: %s
                
                IMPORTANT: Generate %d COMPLETELY DIFFERENT questions from the real exam.
                The following question topics/texts already exist in the real exam — DO NOT repeat them:
                %s
                
                Generate fresh questions covering DIFFERENT subtopics, scenarios, and angles.
                Same difficulty level, but new content the student hasn't seen before.
                orderIndex starts at %d and increments by 1.
                
                Return ONLY valid JSON array (no markdown):
                [
                  {
                    "type": "MCQ",
                    "questionText": "...",
                    "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
                    "expectedAnswer": "B",
                    "points": 2,
                    "orderIndex": %d
                  },
                  ...
                ]
                
                Rules:
                1. Generate exactly %d questions
                2. Mix types: ~60%% MCQ, ~20%% SCENARIO, ~20%% CODE (with options)
                3. Every question MUST have "options" array with 4 items (A/B/C/D)
                4. expectedAnswer = single letter A/B/C/D, shuffle correct position
                5. Questions must be genuinely different from the avoid list above
                """.formatted(topic, category, difficulty, count, avoidList, offset, offset, count);
    }

    // ── LLM call ──────────────────────────────────────────────────────────────

    private String callLlm(String prompt) {
        if (openAiKey == null || openAiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);
        headers.set("HTTP-Referer", "http://localhost:4201");
        headers.set("X-Title", "SkillHub Practice");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.9); // higher temp = more variety
        body.put("max_tokens", 3000);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response: " + e.getMessage());
        }
    }

    // ── Parse ─────────────────────────────────────────────────────────────────

    private List<Map<String, Object>> parseQuestions(String raw, int offset) {
        try {
            String cleaned = raw.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-zA-Z]*\\n?", "").replace("```", "").trim();
            }
            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode arr  = root.isArray() ? root : root.path("questions");

            List<Map<String, Object>> result = new ArrayList<>();
            int idx = offset;
            for (JsonNode q : arr) {
                Map<String, Object> qMap = new LinkedHashMap<>();
                qMap.put("id",             -(idx + 1)); // negative IDs = practice (not real)
                qMap.put("type",           q.path("type").asText("MCQ"));
                qMap.put("questionText",   q.path("questionText").asText(""));
                qMap.put("expectedAnswer", q.path("expectedAnswer").asText(""));
                qMap.put("codeLanguage",   q.path("codeLanguage").asText(null));
                qMap.put("points",         q.path("points").asDouble(2.0));
                qMap.put("orderIndex",     idx);
                qMap.put("choices",        List.of());

                List<String> opts = new ArrayList<>();
                if (q.has("options") && q.path("options").isArray()) {
                    for (JsonNode opt : q.path("options")) opts.add(opt.asText());
                }
                qMap.put("options", opts);
                result.add(qMap);
                idx++;
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse practice questions: " + e.getMessage());
        }
    }
}
