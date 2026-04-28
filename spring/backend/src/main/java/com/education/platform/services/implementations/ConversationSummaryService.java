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
import java.util.Comparator;
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
        String fallback = summarizeWithFallback(meUsername, otherUsername, lines);
        try {
            if ("ollama".equals(provider)) {
                String summary = summarizeWithOllama(meUsername, otherUsername, lines);
                if (!summary.isBlank()) {
                    String normalized = normalizeSummaryText(summary);
                    if (isAcceptableSummary(normalized, lines)) {
                        return normalized;
                    }
                }
            } else if ("openai".equals(provider) && openAiApiKey != null && !openAiApiKey.isBlank()) {
                String summary = summarizeWithOpenAi(meUsername, otherUsername, lines);
                if (!summary.isBlank()) {
                    String normalized = normalizeSummaryText(summary);
                    if (isAcceptableSummary(normalized, lines)) {
                        return normalized;
                    }
                }
            }
        } catch (Exception ignored) {
            // Keep feature available even without LLM runtime.
        }
        return fallback;
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
        return "You are summarizing a private text chat between " + meUsername + " and " + otherUsername + ".\n"
                + "Important constraints:\n"
                + "- Voice messages are already removed.\n"
                + "- Do NOT rewrite the conversation line-by-line.\n"
                + "- Do NOT include email addresses, usernames, or direct quotes.\n"
                + "- Keep it concise, factual, and useful.\n"
                + "Output format:\n"
                + "Key points:\n"
                + "- ...\n"
                + "- ...\n"
                + "- ...\n"
                + "Action items:\n"
                + "- ...\n"
                + "Open question: ...\n\n"
                + "Transcript:\n" + transcript;
    }

    private String summarizeWithFallback(String meUsername, String otherUsername, List<String> lines) {
        Map<String, Integer> keywords = new HashMap<>();
        List<String> lastUserQuestions = new ArrayList<>();
        List<String> lastAssistantActions = new ArrayList<>();

        for (String line : lines) {
            String cleaned = line.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
            for (String token : cleaned.split("\\s+")) {
                if (token.length() < 4 || STOP_WORDS.contains(token)) {
                    continue;
                }
                keywords.merge(token, 1, Integer::sum);
            }

            String[] parts = splitSpeakerAndText(line);
            String speaker = parts[0];
            String text = parts[1];
            if (text.isBlank()) {
                continue;
            }
            String lowered = text.toLowerCase(Locale.ROOT);
            if (speaker.equals(meUsername) && text.contains("?")) {
                lastUserQuestions.add(compact(text, 120));
                if (lastUserQuestions.size() > 3) {
                    lastUserQuestions.remove(0);
                }
            }
            if (speaker.equals(otherUsername) && looksLikeAction(lowered)) {
                lastAssistantActions.add(compact(text, 120));
                if (lastAssistantActions.size() > 3) {
                    lastAssistantActions.remove(0);
                }
            }
        }
        List<String> topWords = keywords.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue()).reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        int questionCount = 0;
        int actionCount = 0;
        int decisionCount = 0;
        int issueCount = 0;
        for (String line : lines) {
            String l = line == null ? "" : line.toLowerCase(Locale.ROOT).trim();
            if (l.isBlank()) {
                continue;
            }
            if (l.contains("?")) {
                questionCount++;
            }
            if (l.matches(".*\\b(please|do it|fix|update|add|send|create|implement)\\b.*")) {
                actionCount++;
            }
            if (l.matches(".*\\b(done|ok|yes|accepted|resolved|works|working)\\b.*")) {
                decisionCount++;
            }
            if (l.matches(".*\\b(error|failed|issue|problem|bug|exception|404|500|truncation)\\b.*")) {
                issueCount++;
            }
        }

        StringBuilder out = new StringBuilder();
        out.append("Key points:\n");
        if (!topWords.isEmpty()) {
            out.append("- Main topics discussed: ").append(String.join(", ", topWords)).append(".\n");
        }
        if (issueCount > 0) {
            out.append("- The conversation includes troubleshooting and error-resolution steps.\n");
        }
        if (decisionCount > 0) {
            out.append("- At least one decision/confirmation was made during the exchange.\n");
        }

        out.append("Action items:\n");
        if (actionCount > 0) {
            out.append("- Follow through on the requested implementation/fix and validate result.\n");
        } else {
            out.append("- Clarify the exact expected outcome and next technical step.\n");
        }
        if (!lastAssistantActions.isEmpty()) {
            out.append("- Latest proposed action: ").append(lastAssistantActions.get(lastAssistantActions.size() - 1)).append("\n");
        }
        out.append("Open question: ");
        if (!lastUserQuestions.isEmpty()) {
            out.append(lastUserQuestions.get(lastUserQuestions.size() - 1));
        } else if (questionCount > 0) {
            out.append("Confirm whether the latest issue is fully resolved after the change.");
        } else {
            out.append("What is the next priority task to execute in this thread?");
        }

        return out.toString().trim();
    }

    private String normalizeSummaryText(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.isBlank()) {
            return "";
        }
        text = text.replaceAll("\\r\\n?", "\n");
        text = text.replaceAll("(?im)^summary\\s*:?\\s*", "");

        String[] lines = text.split("\n");
        List<String> clean = new ArrayList<>();
        for (String line : lines) {
            String l = line.trim();
            if (l.isBlank()) {
                continue;
            }
            // avoid transcript-like rows "name: message"
            if (l.matches("^[^\\s:]{2,40}:\\s+.*")) {
                continue;
            }
            // avoid leaking emails in summary card
            l = l.replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[email]");
            clean.add(l);
        }
        if (clean.isEmpty()) {
            return "Key points:\n- The conversation contains short exchanges with limited context.\nAction items:\n- Ask one clarifying question before proceeding.\nOpen question: What should be done next?";
        }
        return String.join("\n", clean);
    }

    private boolean isAcceptableSummary(String summary, List<String> lines) {
        if (summary == null || summary.isBlank()) {
            return false;
        }
        String s = summary.toLowerCase(Locale.ROOT);
        if (!s.contains("key points") && !s.contains("action items") && !s.contains("open question")) {
            return false;
        }

        int echoedLines = 0;
        int checked = 0;
        for (String line : lines) {
            String[] parts = splitSpeakerAndText(line);
            String text = parts[1];
            if (text.length() < 12) {
                continue;
            }
            checked++;
            String probe = text.length() > 60 ? text.substring(0, 60).toLowerCase(Locale.ROOT) : text.toLowerCase(Locale.ROOT);
            if (s.contains(probe)) {
                echoedLines++;
            }
            if (checked >= 12) {
                break;
            }
        }
        // Reject transcript-like summaries that copy too many original lines.
        return checked == 0 || ((double) echoedLines / checked) < 0.34;
    }

    private String[] splitSpeakerAndText(String line) {
        String v = line == null ? "" : line.trim();
        int i = v.indexOf(':');
        if (i <= 0 || i >= v.length() - 1) {
            return new String[]{"", v};
        }
        return new String[]{v.substring(0, i).trim(), v.substring(i + 1).trim()};
    }

    private boolean looksLikeAction(String lowered) {
        return lowered.contains("i will")
                || lowered.contains("i'll")
                || lowered.contains("done")
                || lowered.contains("fixed")
                || lowered.contains("updated")
                || lowered.contains("changed")
                || lowered.contains("added");
    }

    private String compact(String text, int max) {
        String t = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (t.length() <= max) {
            return t;
        }
        return t.substring(0, max) + "...";
    }
}

