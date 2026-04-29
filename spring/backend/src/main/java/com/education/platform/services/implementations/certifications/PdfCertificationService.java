package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.education.platform.entities.certifications.Certification;
import com.education.platform.entities.certifications.Question;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PdfCertificationService {

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openAiUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    private final CertificationService certificationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts text from PDF, sends it to LLM to structure it as a certification,
     * then persists and returns the result.
     */
    public CertificationDTO convertPdfAndSave(MultipartFile file) throws IOException {
        String pdfText = extractText(file);
        String rawJson = callOpenAiWithPdfContent(pdfText);
        CertificationCreateDTO dto = parseGeneratedJson(rawJson);
        return certificationService.create(dto);
    }

    // ─── PDF TEXT EXTRACTION ─────────────────────────────────────────────────

    private String extractText(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            // Truncate to avoid token limits (~12000 chars ≈ 3000 tokens)
            return text.length() > 12000 ? text.substring(0, 12000) : text;
        }
    }

    // ─── LLM CALL ────────────────────────────────────────────────────────────

    private String callOpenAiWithPdfContent(String pdfText) {
        if (openAiKey == null || openAiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key not configured. Set openai.api.key in application.properties");
        }

        String prompt = """
                You are an expert certification designer. The following text was extracted from a PDF document.
                Convert it into a structured certification exam in JSON format.
                
                PDF Content:
                ---
                %s
                ---
                
                Return ONLY valid JSON (no markdown, no explanation) matching this exact structure:
                {
                  "title": "...",
                  "description": "...",
                  "category": "...",
                  "difficulty": "BEGINNER|INTERMEDIATE|ADVANCED",
                  "examTitle": "...",
                  "timeLimitMinutes": 60,
                  "questions": [
                    {
                      "type": "FILL_BLANK|MATCH|CODE|EXPLAIN|WRITE",
                      "questionText": "...",
                      "points": 10,
                      "orderIndex": 1,
                      "expectedAnswer": "...",
                      "codeLanguage": "java|python|javascript|null",
                      "matchPairs": [{"left": "...", "right": "..."}]
                    }
                  ]
                }
                
                Rules:
                - Extract or infer the certification title, description, and category from the content
                - Create meaningful questions based on the content
                - Use a mix of FILL_BLANK, MATCH, CODE, EXPLAIN, WRITE question types
                - Do NOT use MCQ or multiple choice
                - For MATCH type, provide matchPairs with at least 4 pairs
                """.formatted(pdfText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);
        headers.set("HTTP-Referer", "http://localhost:4200");
        headers.set("X-Title", "SkillHub");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.5);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || choices.isEmpty()) {
                throw new RuntimeException("LLM returned no choices. Full response: " + response.getBody());
            }
            return choices.get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage());
        }
    }

    // ─── JSON PARSING ────────────────────────────────────────────────────────

    private CertificationCreateDTO parseGeneratedJson(String rawJson) {
        try {
            String cleaned = rawJson.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-z]*\\n?", "").replace("```", "").trim();
            }

            JsonNode root = objectMapper.readTree(cleaned);
            List<QuestionCreateDTO> questions = new ArrayList<>();

            for (JsonNode qNode : root.path("questions")) {
                List<MatchPairDTO> pairs = new ArrayList<>();
                if (qNode.has("matchPairs")) {
                    for (JsonNode pair : qNode.path("matchPairs")) {
                        String left  = pair.has("left")  ? pair.path("left").asText()  : pair.path("key").asText();
                        String right = pair.has("right") ? pair.path("right").asText() : pair.path("value").asText();
                        pairs.add(new MatchPairDTO(left, right));
                    }
                }
                List<String> options = null;
                if (qNode.has("options") && qNode.path("options").isArray()) {
                    options = new java.util.ArrayList<>();
                    for (JsonNode opt : qNode.path("options")) options.add(opt.asText());
                }
                String typeStr = qNode.path("type").asText("MCQ");
                Question.QuestionType qType;
                try { qType = Question.QuestionType.valueOf(typeStr); }
                catch (IllegalArgumentException e) { qType = Question.QuestionType.MCQ; }

                questions.add(new QuestionCreateDTO(
                        qType,
                        qNode.path("questionText").asText(),
                        qNode.path("points").asDouble(2.0),
                        qNode.path("orderIndex").asInt(0),
                        qNode.path("expectedAnswer").asText(null),
                        qNode.path("codeLanguage").asText(null),
                        pairs.isEmpty() ? null : pairs,
                        options
                ));
            }

            String difficultyStr = root.path("difficulty").asText("INTERMEDIATE");
            Certification.Difficulty difficulty;
            try {
                difficulty = Certification.Difficulty.valueOf(difficultyStr);
            } catch (Exception e) {
                difficulty = Certification.Difficulty.INTERMEDIATE;
            }

            ExamCreateDTO exam = new ExamCreateDTO(
                    root.path("examTitle").asText("Exam"),
                    root.path("timeLimitMinutes").asInt(60),
                    70.0,
                    questions
            );

            return new CertificationCreateDTO(
                    root.path("title").asText("Imported Certification"),
                    root.path("description").asText(""),
                    root.path("category").asText("General"),
                    difficulty,
                    Certification.Status.DRAFT,
                    BigDecimal.ZERO,
                    null, null,
                    root.path("timeLimitMinutes").asInt(60),
                    70.0,
                    List.of(exam),
                    null  // coverImageUrl — generated separately
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM-generated certification JSON: " + e.getMessage());
        }
    }
}
