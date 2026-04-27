package com.education.platform.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ConversationSummaryService {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "that", "with", "this", "from", "have", "about", "just", "your",
            "vous", "avec", "pour", "dans", "mais", "pas", "une", "des", "les", "est", "sur"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.ai.provider:ollama}")
    private String aiProvider;

    @Value("${app.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${app.ai.ollama.model:qwen2.5:7b}")
    private String ollamaModel;

    @Value("${app.ai.ollama.timeout-ms:60000}")
    private int ollamaTimeoutMs;

    @Value("${app.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${app.ai.openai.base-url:https://api.openai.com}")
    private String openAiBaseUrl;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${app.ai.openai.timeout-ms:30000}")
    private int openAiTimeoutMs;

    public String summarize(String meUsername, String otherUsername, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "No text messages to summarize yet.";
        }
        String provider = aiProvider == null ? "" : aiProvider.trim().toLowerCase(Locale.ROOT);
        try {
            if ("ollama".equals(provider)) {
                String summary = summarizeWithOllama(meUsername, otherUsername, lines);
                if (!summary.isBlank()) {
                    return summary;
                }
            } else if ("openai".equals(provider) && openAiApiKey != null && !openAiApiKey.isBlank()) {
                String summary = summarizeWithOpenAi(meUsername, otherUsername, lines);
                if (!summary.isBlank()) {
                    return summary;
                }
            }
        } catch (Exception ignored) {
            // Keep feature available even without LLM runtime.
        }
        return summarizeWithFallback(meUsername, otherUsername, lines);
    }

    private String summarizeWithOllama(String meUsername, String otherUsername, List<String> lines)
            throws IOException, InterruptedException {
        String prompt = buildPrompt(meUsername, otherUsername, lines);
        Map<String, Object> body = Map.of(
                "model", ollamaModel,
                "prompt", prompt,
                "stream", false
        );
        String payload = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaBaseUrl + "/api/generate"))
                .timeout(Duration.ofMillis(Math.max(5000, ollamaTimeoutMs)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Ollama summary failed with status " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        return root.path("response").asText("").trim();
    }

    private String summarizeWithOpenAi(String meUsername, String otherUsername, List<String> lines)
            throws IOException, InterruptedException {
        String prompt = buildPrompt(meUsername, otherUsername, lines);
        Map<String, Object> body = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "You summarize private user chats clearly and briefly."),
                        Map.of("role", "user", "content", prompt)
                )
        );
        String payload = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openAiBaseUrl + "/v1/chat/completions"))
                .timeout(Duration.ofMillis(Math.max(5000, openAiTimeoutMs)))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenAI summary failed with status " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").path(0).path("message").path("content").asText("").trim();
    }

    private String buildPrompt(String meUsername, String otherUsername, List<String> lines) {
        List<String> clipped = lines.size() > 80 ? lines.subList(lines.size() - 80, lines.size()) : lines;
        StringBuilder transcript = new StringBuilder();
        for (String line : clipped) {
            transcript.append("- ").append(line).append("\n");
        }
        return "Summarize this private chat between " + meUsername + " and " + otherUsername + ".\n"
                + "Important: voice messages are already removed. Keep the summary factual.\n"
                + "Output format:\n"
                + "1) 3-5 short bullet points (key topics and decisions)\n"
                + "2) one final line: \"Next likely action: ...\"\n\n"
                + "Transcript:\n" + transcript;
    }

    private String summarizeWithFallback(String meUsername, String otherUsername, List<String> lines) {
        List<String> recent = lines.size() > 6 ? lines.subList(lines.size() - 6, lines.size()) : lines;
        Map<String, Integer> keywords = new HashMap<>();
        for (String line : lines) {
            String cleaned = line.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
            for (String token : cleaned.split("\\s+")) {
                if (token.length() < 4 || STOP_WORDS.contains(token)) {
                    continue;
                }
                keywords.merge(token, 1, Integer::sum);
            }
        }
        List<String> topWords = keywords.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
        Set<String> bullets = new LinkedHashSet<>();
        for (String line : recent) {
            String trimmed = line.trim();
            if (!trimmed.isBlank()) {
                bullets.add(trimmed.length() <= 120 ? trimmed : trimmed.substring(0, 120) + "...");
            }
        }
        StringBuilder out = new StringBuilder();
        out.append("Summary for ").append(meUsername).append(" and ").append(otherUsername).append(":\n");
        if (!topWords.isEmpty()) {
            out.append("- Frequent topics: ").append(String.join(", ", topWords)).append(".\n");
        }
        int count = 0;
        for (String bullet : bullets) {
            out.append("- ").append(bullet).append("\n");
            count++;
            if (count >= 4) {
                break;
            }
        }
        out.append("Next likely action: follow up on the latest open question in chat.");
        return out.toString().trim();
    }
}

