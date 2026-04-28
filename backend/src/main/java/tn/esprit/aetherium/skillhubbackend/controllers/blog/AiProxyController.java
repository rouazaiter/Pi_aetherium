package tn.esprit.aetherium.skillhubbackend.controllers.blog;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiProxyController {

    private static final String GROQ_KEY = "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";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "prompt is required"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(GROQ_KEY);

            Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 400,
                "temperature", 0.4
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_URL, request, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String text = (String) message.get("content");

            return ResponseEntity.ok(Map.of("text", text.trim()));
        } catch (Exception e) {
            System.err.println("[AI Proxy] Error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
