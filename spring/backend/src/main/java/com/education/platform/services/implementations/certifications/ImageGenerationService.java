package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openAiUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateCoverImageUrl(String title, String category, String difficulty) {
        String prompt = buildImagePrompt(title, category, difficulty);
        return buildPollinationsUrl(prompt, title);
    }

    // ── LLM prompt generation ─────────────────────────────────────────────────

    private String buildImagePrompt(String title, String category, String difficulty) {
        if (openAiKey == null || openAiKey.isBlank()) {
            return buildFallbackPrompt(title, category, difficulty);
        }

        String userMessage = """
            Write a concise image generation prompt (max 40 words) for a certification cover image.

            Certification: %s
            Category: %s
            Difficulty: %s

            Requirements:
            - 3D render style, dark navy background, neon lighting
            - No text, no letters anywhere
            - Represent the topic with symbolic 3D objects
            - Cinematic, dramatic lighting, 8K quality

            Return ONLY the prompt, nothing else. Keep it under 40 words.
            """.formatted(title, category != null ? category : "Technology", difficulty);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiKey);
            headers.set("HTTP-Referer", "http://localhost:4200");
            headers.set("X-Title", "SkillHub");

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.85);
            body.put("max_tokens", 120);
            body.put("messages", List.of(Map.of("role", "user", "content", userMessage)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String llmPrompt = root.path("choices").get(0).path("message").path("content").asText().trim();
            llmPrompt = llmPrompt.replaceAll("^[\"']|[\"']$", "").trim();

            return llmPrompt.isEmpty() ? buildFallbackPrompt(title, category, difficulty) : llmPrompt;

        } catch (Exception e) {
            System.err.println("[ImageGen] LLM prompt failed: " + e.getMessage());
            return buildFallbackPrompt(title, category, difficulty);
        }
    }

    // ── Rich fallback prompts per category ────────────────────────────────────

    private String buildFallbackPrompt(String title, String category, String difficulty) {
        String diffStyle = switch (difficulty != null ? difficulty.toUpperCase() : "INTERMEDIATE") {
            case "BEGINNER"  -> "teal and green neon, warm lighting";
            case "ADVANCED"  -> "crimson and purple neon, dramatic shadows";
            default          -> "electric blue and cyan neon, professional lighting";
        };

        String base = "3D render, dark navy background, volumetric light, cinematic, 8K, no text";

        if (category == null) return "glowing tech orb, " + diffStyle + ", " + base;

        String cat = category.toLowerCase();

        if (cat.contains("java"))
            return "3D glowing Java coffee cup, floating code fragments, binary streams, " + diffStyle + ", " + base;
        if (cat.contains("spring"))
            return "3D glowing spring coil, API network nodes, microservices, " + diffStyle + ", " + base;
        if (cat.contains("python"))
            return "3D python snake made of circuit patterns, data sphere, " + diffStyle + ", " + base;
        if (cat.contains("javascript") || cat.contains("js"))
            return "3D JavaScript logo, floating UI components, async loops, yellow neon, " + base;
        if (cat.contains("react"))
            return "3D React atom symbol, glowing electron orbits, component cards, " + diffStyle + ", " + base;
        if (cat.contains("angular"))
            return "3D Angular shield crystal, dependency injection nodes, " + diffStyle + ", " + base;
        if (cat.contains("web") || cat.contains("html") || cat.contains("css"))
            return "3D browser window in space, glowing HTML tags, CSS grid lines, " + diffStyle + ", " + base;
        if (cat.contains("docker") || cat.contains("devops") || cat.contains("kubernetes"))
            return "3D glowing container boxes, deployment pipeline, whale silhouette, " + diffStyle + ", " + base;
        if (cat.contains("sql") || cat.contains("database"))
            return "3D crystal database cylinders, data flow streams, query visualization, " + diffStyle + ", " + base;
        if (cat.contains("cloud") || cat.contains("aws") || cat.contains("azure"))
            return "3D glowing cloud infrastructure, server nodes, data centers, " + diffStyle + ", " + base;
        if (cat.contains("security") || cat.contains("cyber"))
            return "3D glowing shield, circuit patterns, binary streams, lock symbols, " + diffStyle + ", " + base;
        if (cat.contains("math") || cat.contains("data") || cat.contains("ml") || cat.contains("ai"))
            return "3D neural network, glowing nodes, synaptic connections, equations, " + diffStyle + ", " + base;
        if (cat.contains("php"))
            return "3D PHP elephant, purple crystal, server request flows, " + diffStyle + ", " + base;

        return "3D glowing tech sphere, circuit patterns, " + title + " concept, " + diffStyle + ", " + base;
    }

    // ── Pollinations URL ──────────────────────────────────────────────────────

    private String buildPollinationsUrl(String prompt, String title) {
        try {
            // Keep prompt short — Pollinations works best under 200 chars
            String cleanPrompt = prompt.trim();
            if (cleanPrompt.length() > 200) {
                cleanPrompt = cleanPrompt.substring(0, 200);
            }

            String encoded = URLEncoder.encode(cleanPrompt, StandardCharsets.UTF_8);
            int seed = Math.abs(title.hashCode() % 99999);

            // Use the stable image.pollinations.ai endpoint — no auth, no model param
            return "https://image.pollinations.ai/prompt/" + encoded
                    + "?width=800&height=450&seed=" + seed + "&nologo=true";
        } catch (Exception e) {
            return null;
        }
    }
}
