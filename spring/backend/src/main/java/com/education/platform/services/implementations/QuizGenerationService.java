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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class QuizGenerationService {
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

    public GeneratedQuizQuestion generateQuestion(String topic) {
        return generateQuestions(topic, 1).get(0);
    }

    public List<GeneratedQuizQuestion> generateQuestions(String topic, int count) {
        int safeCount = Math.max(1, count);
        String provider = aiProvider == null ? "" : aiProvider.trim().toLowerCase(Locale.ROOT);

        try {
            if ("ollama".equals(provider)) {
                List<GeneratedQuizQuestion> llmQuestions = generateWithOllama(topic, safeCount);
                if (llmQuestions.size() == safeCount) {
                    return llmQuestions;
                }
            } else if ("openai".equals(provider) && openAiApiKey != null && !openAiApiKey.isBlank()) {
                List<GeneratedQuizQuestion> llmQuestions = generateWithOpenAi(topic, safeCount);
                if (llmQuestions.size() == safeCount) {
                    return llmQuestions;
                }
            }
        } catch (Exception ignored) {
            // If LLM call fails, fallback keeps quiz feature available.
        }

        List<GeneratedQuizQuestion> questions = new ArrayList<>(safeCount);
        for (int i = 0; i < safeCount; i++) {
            questions.add(generateTemplateQuestion(topic));
        }
        return questions;
    }

    private List<GeneratedQuizQuestion> generateWithOllama(String topic, int count) throws IOException, InterruptedException {
        String safeTopic = (topic == null || topic.isBlank()) ? "general learning" : topic.trim();
        String prompt = "Generate " + count + " unique multiple-choice quiz questions about \"" + safeTopic + "\". "
                + "Return ONLY strict JSON with this exact shape: "
                + "{\"questions\":[{\"question\":\"...\",\"optionA\":\"...\",\"optionB\":\"...\",\"optionC\":\"...\",\"optionD\":\"...\",\"correctOption\":\"A|B|C|D\"}]}. "
                + "Rules: options must be short, clear, and exactly one correct option per question.";

        Map<String, Object> body = Map.of(
                "model", ollamaModel,
                "prompt", prompt,
                "stream", false,
                "format", "json",
                "options", Map.of("temperature", 0.8)
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
            throw new IOException("Ollama request failed with status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("response").asText("");
        if (content.isBlank()) {
            throw new IOException("Ollama returned empty content");
        }
        return parseQuestionsFromJsonContent(content, count, "Ollama");
    }

    private List<GeneratedQuizQuestion> generateWithOpenAi(String topic, int count) throws IOException, InterruptedException {
        String safeTopic = (topic == null || topic.isBlank()) ? "general learning" : topic.trim();
        String prompt = "Generate " + count + " unique multiple-choice quiz questions about \"" + safeTopic + "\". "
                + "Return ONLY strict JSON with this exact shape: "
                + "{\"questions\":[{\"question\":\"...\",\"optionA\":\"...\",\"optionB\":\"...\",\"optionC\":\"...\",\"optionD\":\"...\",\"correctOption\":\"A|B|C|D\"}]}. "
                + "Rules: options must be short, clear, and exactly one correct option per question.";

        Map<String, Object> body = Map.of(
                "model", openAiModel,
                "temperature", 0.8,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an expert quiz generator."),
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
            throw new IOException("OpenAI request failed with status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("choices").path(0).path("message").path("content").asText("");
        if (content.isBlank()) {
            throw new IOException("OpenAI returned empty content");
        }
        return parseQuestionsFromJsonContent(content, count, "OpenAI");
    }

    private List<GeneratedQuizQuestion> parseQuestionsFromJsonContent(String jsonContent, int expectedCount, String providerName) throws IOException {
        JsonNode generatedRoot = objectMapper.readTree(jsonContent);
        JsonNode questionsNode = generatedRoot.path("questions");
        if (!questionsNode.isArray()) {
            throw new IOException(providerName + " JSON does not contain questions array");
        }

        List<GeneratedQuizQuestion> result = new ArrayList<>();
        for (JsonNode node : questionsNode) {
            String question = node.path("question").asText("").trim();
            String optionA = node.path("optionA").asText("").trim();
            String optionB = node.path("optionB").asText("").trim();
            String optionC = node.path("optionC").asText("").trim();
            String optionD = node.path("optionD").asText("").trim();
            String correctOption = node.path("correctOption").asText("").trim().toUpperCase(Locale.ROOT);
            if (question.isEmpty() || optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty()) {
                continue;
            }
            if (!correctOption.equals("A") && !correctOption.equals("B") && !correctOption.equals("C") && !correctOption.equals("D")) {
                continue;
            }
            result.add(new GeneratedQuizQuestion(question, optionA, optionB, optionC, optionD, correctOption));
        }
        if (result.size() < expectedCount) {
            throw new IOException(providerName + " returned insufficient valid questions");
        }
        return result.subList(0, expectedCount);
    }

    private GeneratedQuizQuestion generateTemplateQuestion(String topic) {
        String normalizedTopic = topic == null ? "" : topic.trim().toLowerCase(Locale.ROOT);
        if (normalizedTopic.contains("java")) {
            return pick(javaQuestions());
        }
        if (normalizedTopic.contains("python")) {
            return pick(pythonQuestions());
        }
        if (normalizedTopic.contains("javascript") || normalizedTopic.contains("js")) {
            return pick(javaScriptQuestions());
        }
        return pick(generalQuestions(topic));
    }

    private GeneratedQuizQuestion pick(List<GeneratedQuizQuestion> questions) {
        return questions.get(ThreadLocalRandom.current().nextInt(questions.size()));
    }

    private List<GeneratedQuizQuestion> javaQuestions() {
        return List.of(
                new GeneratedQuizQuestion(
                        "In Java, which keyword is used to inherit a class?",
                        "extends",
                        "implements",
                        "inherits",
                        "super",
                        "A"),
                new GeneratedQuizQuestion(
                        "Which Java collection does NOT allow duplicate values?",
                        "List",
                        "Map",
                        "Set",
                        "Queue",
                        "C"),
                new GeneratedQuizQuestion(
                        "What does JVM stand for?",
                        "Java Variable Machine",
                        "Java Virtual Machine",
                        "Just Verified Module",
                        "Java Visual Model",
                        "B")
        );
    }

    private List<GeneratedQuizQuestion> pythonQuestions() {
        return List.of(
                new GeneratedQuizQuestion(
                        "In Python, which keyword defines a function?",
                        "func",
                        "define",
                        "lambda",
                        "def",
                        "D"),
                new GeneratedQuizQuestion(
                        "Which data type is immutable in Python?",
                        "list",
                        "dict",
                        "tuple",
                        "set",
                        "C"),
                new GeneratedQuizQuestion(
                        "How do you start a comment line in Python?",
                        "//",
                        "#",
                        "--",
                        "/*",
                        "B")
        );
    }

    private List<GeneratedQuizQuestion> javaScriptQuestions() {
        return List.of(
                new GeneratedQuizQuestion(
                        "Which keyword declares a block-scoped variable in JavaScript?",
                        "var",
                        "let",
                        "define",
                        "scope",
                        "B"),
                new GeneratedQuizQuestion(
                        "Which array method creates a new array from transformed elements?",
                        "filter()",
                        "reduce()",
                        "map()",
                        "forEach()",
                        "C"),
                new GeneratedQuizQuestion(
                        "What is the result type of JSON.parse()?",
                        "String",
                        "Boolean",
                        "JavaScript object/value",
                        "ArrayBuffer only",
                        "C")
        );
    }

    private List<GeneratedQuizQuestion> generalQuestions(String topic) {
        String safeTopic = (topic == null || topic.isBlank()) ? "this topic" : topic.trim();
        return List.of(
                new GeneratedQuizQuestion(
                        "Which option best describes a key concept in " + safeTopic + "?",
                        "Only memorization matters",
                        "Practice and understanding both matter",
                        "Theory is never useful",
                        "There are no fundamentals",
                        "B"),
                new GeneratedQuizQuestion(
                        "To improve in " + safeTopic + ", what is usually most effective?",
                        "Random guessing",
                        "Consistent practice with feedback",
                        "Ignoring mistakes",
                        "Avoiding exercises",
                        "B"),
                new GeneratedQuizQuestion(
                        "What helps most when learning " + safeTopic + " collaboratively?",
                        "Not sharing ideas",
                        "Skipping discussion",
                        "Explaining solutions to peers",
                        "Working alone forever",
                        "C")
        );
    }

    public record GeneratedQuizQuestion(
            String question,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption
    ) {
    }
}
