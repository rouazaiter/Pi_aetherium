package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.education.platform.entities.certifications.Certification;
import com.education.platform.repositories.certifications.CertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatAssistantController {

    private final CertificationRepository certificationRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openAiUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    // ── Request / Response records ────────────────────────────────────────────

    record ChatMessage(String role, String content) {}
    record ChatRequest(List<ChatMessage> messages) {}
    record ChatResponse(String reply, List<CertCard> recommendations) {}
    record CertCard(Long id, String title, String category, String difficulty,
                    double price, String description, String coverImageUrl) {}

    // ── POST /api/chat/assistant ──────────────────────────────────────────────

    @PostMapping("/api/chat/assistant")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req) {
        try {
            // Build catalog context from published certifications
            List<Certification> certs = certificationRepository.findAll().stream()
                    .filter(c -> c.getStatus() == Certification.Status.PUBLISHED)
                    .collect(Collectors.toList());

            String catalogContext = buildCatalogContext(certs);
            String systemPrompt   = buildSystemPrompt(catalogContext);

            // Build messages for LLM
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            for (ChatMessage m : req.messages()) {
                messages.add(Map.of("role", m.role(), "content", m.content()));
            }

            // Call LLM
            String rawReply = callLlm(messages);

            // Parse reply — may contain JSON with recommendations
            ChatResponse response = parseReply(rawReply, certs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse(
                    "I'm having trouble connecting right now. Please try again in a moment.",
                    List.of()
            ));
        }
    }

    // ── System prompt ─────────────────────────────────────────────────────────

    private String buildSystemPrompt(String catalogContext) {
        return """
                You are SkillHub's Certification Guide Assistant — a smart, friendly AI advisor
                embedded in the SkillHub certification store.

                YOUR ROLE:
                - Help users choose the right certification based on their goals
                - Explain certifications clearly (what it is, difficulty, skills gained, who it's for)
                - Compare certifications when asked
                - Suggest learning paths (beginner → advanced)
                - Identify skill gaps and recommend certifications to fill them

                STRICT RULES:
                - ONLY discuss certifications, skills, learning paths, and career goals
                - NEVER answer questions unrelated to certifications or learning
                - Keep answers concise (max 3-4 sentences per point)
                - Be warm, encouraging, and professional
                - When recommending certifications, ALWAYS include a JSON block at the end

                CATALOG (current published certifications):
                %s

                RESPONSE FORMAT:
                When you recommend specific certifications, end your message with this exact JSON block:
                ```recommendations
                ["CERT_TITLE_1", "CERT_TITLE_2"]
                ```
                Use exact certification titles from the catalog above.
                If no specific recommendation is needed, omit the JSON block entirely.

                QUICK ACTIONS the user may click:
                - "Recommend for me" → ask about their goal, then recommend
                - "Compare certifications" → ask which two to compare
                - "Beginner path" → list beginner certifications in order
                - "Advanced path" → list advanced certifications
                """.formatted(catalogContext);
    }

    private String buildCatalogContext(List<Certification> certs) {
        if (certs.isEmpty()) return "No certifications currently available.";
        return certs.stream().map(c -> {
            // Access fields directly via reflection-safe approach
            String cat   = c.getCategory()      != null ? c.getCategory()      : "General";
            String diff  = c.getDifficulty()    != null ? c.getDifficulty().name() : "N/A";
            String price = c.getPrice()         != null && c.getPrice().doubleValue() > 0
                           ? "$" + c.getPrice().toPlainString() : "FREE";
            int    dur   = c.getDurationMinutes() != null ? c.getDurationMinutes() : 60;
            double pass  = c.getPassingScore()  != null ? c.getPassingScore()  : 70.0;
            String desc  = c.getDescription()   != null
                           ? c.getDescription().substring(0, Math.min(120, c.getDescription().length())) + "…"
                           : "";
            return String.format("- %s | Category: %s | Difficulty: %s | Price: %s | Duration: %d min | Passing: %.0f%% | %s",
                    c.getTitle(), cat, diff, price, dur, pass, desc);
        }).collect(Collectors.joining("\n"));
    }

    // ── LLM call ──────────────────────────────────────────────────────────────

    private String callLlm(List<Map<String, String>> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);
        headers.set("HTTP-Referer", "http://localhost:4201");
        headers.set("X-Title", "SkillHub Chat");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.7);
        body.put("max_tokens", 600);
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response");
        }
    }

    // ── Parse reply + extract recommendations ─────────────────────────────────

    private ChatResponse parseReply(String raw, List<Certification> certs) {
        List<CertCard> recommendations = new ArrayList<>();
        String cleanReply = raw;

        // Extract ```recommendations [...] ``` block
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "```recommendations\\s*\\n([\\s\\S]*?)\\n```",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher m = p.matcher(raw);
        if (m.find()) {
            String jsonArray = m.group(1).trim();
            cleanReply = raw.substring(0, m.start()).trim();
            try {
                JsonNode arr = objectMapper.readTree(jsonArray);
                if (arr.isArray()) {
                    for (JsonNode node : arr) {
                        String title = node.asText();
                        certs.stream()
                                .filter(c -> c.getTitle().equalsIgnoreCase(title))
                                .findFirst()
                                .ifPresent(c -> recommendations.add(new CertCard(
                                        c.getId(), c.getTitle(),
                                        c.getCategory() != null ? c.getCategory() : "General",
                                        c.getDifficulty() != null ? c.getDifficulty().name() : "N/A",
                                        c.getPrice() != null ? c.getPrice().doubleValue() : 0,
                                        c.getDescription() != null
                                                ? c.getDescription().substring(0, Math.min(100, c.getDescription().length())) + "…"
                                                : "",
                                        c.getCoverImageUrl()
                                )));
                    }
                }
            } catch (Exception ignored) {}
        }

        return new ChatResponse(cleanReply, recommendations);
    }
}
