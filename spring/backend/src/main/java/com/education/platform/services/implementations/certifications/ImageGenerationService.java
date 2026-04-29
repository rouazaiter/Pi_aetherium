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
            Write a vivid image generation prompt (max 50 words) for a professional certification course cover.

            Certification title: %s
            Category: %s
            Difficulty: %s

            Style rules:
            - Photorealistic 3D render, dark navy/midnight blue background
            - Dramatic neon lighting matching the topic color palette
            - Central symbolic 3D object representing the technology/skill
            - Floating particles, light rays, depth of field
            - Absolutely NO text, NO letters, NO numbers, NO words
            - Cinematic composition, 8K ultra-detailed

            Return ONLY the image prompt text. No explanation. Max 50 words.
            """.formatted(
                title,
                category != null ? category : "Technology",
                difficulty != null ? difficulty : "INTERMEDIATE"
            );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiKey);
            headers.set("HTTP-Referer", "http://localhost:4200");
            headers.set("X-Title", "SkillHub");

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.9);
            body.put("max_tokens", 150);
            body.put("messages", List.of(Map.of("role", "user", "content", userMessage)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String llmPrompt = root.path("choices").get(0).path("message").path("content").asText().trim();
            // Strip surrounding quotes if any
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
            case "BEGINNER"  -> "soft teal and emerald neon glow, warm inviting lighting";
            case "ADVANCED"  -> "intense crimson and deep purple neon, dramatic dark shadows, high contrast";
            default          -> "electric blue and cyan neon, sharp professional lighting";
        };

        String base = "photorealistic 3D render, dark midnight navy background, volumetric light rays, "
                    + "floating particles, depth of field, cinematic composition, 8K ultra-detailed, "
                    + "no text no letters no words no numbers";

        if (category == null) {
            return "glowing crystalline tech orb, circuit board patterns, " + diffStyle + ", " + base;
        }

        String cat = category.toLowerCase();

        if (cat.contains("java"))
            return "giant glowing 3D Java coffee cup made of molten metal, "
                 + "floating binary code streams, steaming neon vapor, " + diffStyle + ", " + base;

        if (cat.contains("spring"))
            return "3D metallic spring coil with glowing API nodes orbiting it, "
                 + "microservice hexagons, data flow lines, " + diffStyle + ", " + base;

        if (cat.contains("python"))
            return "3D python snake coiled around a glowing data sphere, "
                 + "circuit scale patterns, neural network background, " + diffStyle + ", " + base;

        if (cat.contains("javascript") || cat.contains("js"))
            return "3D golden JavaScript logo floating in space, "
                 + "async event loop rings, glowing UI component cards, yellow neon, " + base;

        if (cat.contains("react"))
            return "3D React atom with glowing electron orbits, "
                 + "floating component tree, hooks visualization, " + diffStyle + ", " + base;

        if (cat.contains("angular"))
            return "3D Angular shield made of crystal, "
                 + "dependency injection network, glowing red and white neon, " + base;

        if (cat.contains("web") || cat.contains("html") || cat.contains("css"))
            return "3D browser window floating in dark space, "
                 + "glowing HTML tag brackets, CSS grid lines, responsive layout wireframes, " + diffStyle + ", " + base;

        if (cat.contains("docker") || cat.contains("devops") || cat.contains("kubernetes"))
            return "3D glowing container boxes stacked in a pipeline, "
                 + "whale silhouette, Kubernetes wheel, deployment arrows, " + diffStyle + ", " + base;

        if (cat.contains("sql") || cat.contains("database") || cat.contains("db"))
            return "3D crystal database cylinders with glowing data streams flowing between them, "
                 + "query visualization, table grid, " + diffStyle + ", " + base;

        if (cat.contains("cloud") || cat.contains("aws") || cat.contains("azure") || cat.contains("gcp"))
            return "3D glowing cloud infrastructure, floating server nodes, "
                 + "data center towers, network connections, " + diffStyle + ", " + base;

        if (cat.contains("security") || cat.contains("cyber"))
            return "3D glowing shield with circuit patterns, "
                 + "binary streams, padlock symbol, firewall grid, " + diffStyle + ", " + base;

        if (cat.contains("machine learning") || cat.contains("ml") || cat.contains("ai") || cat.contains("deep"))
            return "3D neural network with glowing synaptic nodes, "
                 + "data flow visualization, brain-circuit hybrid, " + diffStyle + ", " + base;

        if (cat.contains("data") || cat.contains("analytics"))
            return "3D bar charts and pie charts made of glowing crystal, "
                 + "data streams, analytics dashboard, " + diffStyle + ", " + base;

        if (cat.contains("php"))
            return "3D PHP elephant made of purple crystal, "
                 + "server request flow arrows, code brackets, " + diffStyle + ", " + base;

        if (cat.contains("mobile") || cat.contains("android") || cat.contains("ios"))
            return "3D smartphone floating in space with glowing app icons orbiting it, "
                 + "touch gesture trails, " + diffStyle + ", " + base;

        if (cat.contains("network") || cat.contains("cisco"))
            return "3D network topology with glowing router nodes, "
                 + "packet flow lines, OSI layer visualization, " + diffStyle + ", " + base;

        if (cat.contains("programming") || cat.contains("code") || cat.contains("software"))
            return "3D glowing code editor floating in space, "
                 + "syntax highlighted brackets, function call graph, " + diffStyle + ", " + base;

        // Generic tech fallback
        return "3D glowing crystalline tech sphere with circuit patterns, "
             + "floating geometric shapes, " + title + " concept visualization, "
             + diffStyle + ", " + base;
    }

    // ── Pollinations URL ──────────────────────────────────────────────────────

    private String buildPollinationsUrl(String prompt, String title) {
        try {
            // Pollinations works best with concise, descriptive prompts under 300 chars
            String cleanPrompt = prompt.trim();
            if (cleanPrompt.length() > 300) {
                cleanPrompt = cleanPrompt.substring(0, 300);
            }

            String encoded = URLEncoder.encode(cleanPrompt, StandardCharsets.UTF_8);
            int seed = Math.abs(title.hashCode() % 99999);

            // Use sana model (currently the only available model on Pollinations)
            // enhance=true lets Pollinations improve the prompt automatically
            return "https://image.pollinations.ai/prompt/" + encoded
                    + "?width=800&height=450"
                    + "&model=sana"
                    + "&seed=" + seed
                    + "&enhance=true"
                    + "&nologo=true"
                    + "&safe=false";
        } catch (Exception e) {
            return null;
        }
    }
}
