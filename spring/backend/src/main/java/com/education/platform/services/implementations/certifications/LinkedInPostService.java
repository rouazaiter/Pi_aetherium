package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.education.platform.entities.certifications.Enrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LinkedInPostService {

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openAiUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generatePost(Enrollment enrollment) {
        String recipientName = resolveDisplayName(enrollment);
        String certTitle     = enrollment.getCertification().getTitle();
        String category      = enrollment.getCertification().getCategory() != null
                ? enrollment.getCertification().getCategory() : "Technology";
        String score         = String.format("%.1f", enrollment.getScore());
        String certId        = "SKH-" + String.format("%06d", enrollment.getId());
        String date          = enrollment.getCompletedAt()
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));

        String prompt = """
                Write a professional and enthusiastic LinkedIn post for someone who just earned a certification.
                
                Details:
                - Name: %s
                - Certification: %s
                - Category: %s
                - Score: %s%%
                - Date: %s
                - Certificate ID: %s
                - Issued by: SkillHub (https://skillhub.io)
                
                Requirements:
                - Start with an engaging hook (emoji + bold statement)
                - Mention the certification name and score naturally
                - Include 2-3 sentences about what this certification means / skills gained
                - Add a call-to-action encouraging others to upskill
                - End with 5-7 relevant hashtags (e.g. #Java #SpringBoot #Certification #SkillHub)
                - Keep it under 280 words
                - Tone: proud, professional, inspiring — not arrogant
                - Do NOT use markdown formatting like ** or ## — plain text only
                - Include the certificate ID at the end: "Certificate ID: %s"
                
                Return ONLY the post text, nothing else.
                """.formatted(recipientName, certTitle, category, score, date, certId, certId);

        return callLlm(prompt);
    }

    private String callLlm(String prompt) {
        if (openAiKey == null || openAiKey.isBlank()) {
            return buildFallbackPost(prompt);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiKey);
            headers.set("HTTP-Referer", "http://localhost:4200");
            headers.set("X-Title", "SkillHub");

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.85);
            body.put("max_tokens", 512);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return buildFallbackPost(prompt);
        }
    }

    private String buildFallbackPost(String prompt) {
        // Extract name and cert from prompt as fallback
        return "I'm thrilled to share that I've just earned a new certification on SkillHub! "
             + "This achievement represents hours of dedication and hard work. "
             + "Continuous learning is the key to staying ahead in today's fast-paced world. "
             + "If you're looking to upskill, check out SkillHub at https://skillhub.io\n\n"
             + "#Certification #SkillHub #Learning #ProfessionalDevelopment #Upskilling";
    }

    private String resolveDisplayName(Enrollment enrollment) {
        String rawName = enrollment.getFullName();
        if (rawName == null || rawName.isBlank()) {
            String email = enrollment.getUserIdentifier();
            rawName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            String[] parts = rawName.replace('.', ' ').replace('_', ' ').split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty())
                    sb.append(Character.toUpperCase(p.charAt(0)))
                      .append(p.substring(1).toLowerCase()).append(" ");
            }
            rawName = sb.toString().trim();
        }
        return rawName;
    }
}
