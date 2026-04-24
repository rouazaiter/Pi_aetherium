package tn.esprit.backend.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.backend.dto.NotificationPriority;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationAiAssistantService {

    private final ObjectMapper objectMapper;

    @Value("${ai.notification.enabled:true}")
    private boolean aiNotificationEnabled;

    @Value("${ai.notification.base-url:https://api.openai.com/v1/chat/completions}")
    private String aiNotificationBaseUrl;

    @Value("${ai.notification.model:gpt-4.1-mini}")
    private String aiNotificationModel;

    @Value("${ai.notification.api-key:}")
    private String aiNotificationApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public NotificationPayload build(
            String type,
            String fallbackMessage,
            String actorName,
            String requestName,
            NotificationPriority fallbackPriority,
            String fallbackAction
    ) {
        if (!aiNotificationEnabled || aiNotificationApiKey == null || aiNotificationApiKey.isBlank()) {
            return localFallback(type, fallbackMessage, actorName, requestName, fallbackPriority, fallbackAction);
        }

        try {
            return fromProvider(type, fallbackMessage, actorName, requestName, fallbackPriority, fallbackAction);
        } catch (Exception ex) {
            return localFallback(type, fallbackMessage, actorName, requestName, fallbackPriority, fallbackAction);
        }
    }

    private NotificationPayload fromProvider(
            String type,
            String fallbackMessage,
            String actorName,
            String requestName,
            NotificationPriority fallbackPriority,
            String fallbackAction
    ) {
        String systemPrompt = """
                You are an AI Notification Assistant for a marketplace platform.
                Your task: produce one short English notification message and an action suggestion.

                Rules:
                - Keep messages in English only.
                - Message length target: 12 to 26 words.
                - Keep meaning accurate. Do not invent facts.
                - Use a professional, clear, concise style.
                - Priorities:
                  - HIGH for application accepted/rejected.
                  - MEDIUM for new application.
                  - LOW for new service request.
                - Return strict JSON only.

                JSON schema:
                {
                  "message": "string",
                  "priority": "HIGH|MEDIUM|LOW",
                  "suggestedAction": "string"
                }
                """;

        String userPrompt = """
                Event type: %s
                Fallback message: %s
                Actor: %s
                Service request: %s
                Fallback priority: %s
                Fallback action: %s
                """.formatted(
                safe(type),
                safe(fallbackMessage),
                safe(actorName),
                safe(requestName),
                fallbackPriority.name(),
                safe(fallbackAction)
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiNotificationModel);
        payload.put("temperature", 0.35);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiNotificationApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(aiNotificationBaseUrl, HttpMethod.POST, entity, String.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("AI notification provider failed", ex);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new IllegalStateException("AI notification provider returned an invalid response");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            String json = extractJson(content);
            JsonNode parsed = objectMapper.readTree(json);

            String message = parsed.path("message").asText(safe(fallbackMessage)).trim();
            String action = parsed.path("suggestedAction").asText(safe(fallbackAction)).trim();
            String priorityRaw = parsed.path("priority").asText(fallbackPriority.name()).toUpperCase(Locale.ROOT);
            NotificationPriority priority = parsePriority(priorityRaw, fallbackPriority);

            if (message.isBlank()) {
                message = fallbackMessage;
            }
            if (action.isBlank()) {
                action = fallbackAction;
            }

            return new NotificationPayload(message, priority, action, true);
        } catch (Exception ex) {
            return localFallback(type, fallbackMessage, actorName, requestName, fallbackPriority, fallbackAction);
        }
    }

    private NotificationPayload localFallback(
            String type,
            String fallbackMessage,
            String actorName,
            String requestName,
            NotificationPriority fallbackPriority,
            String fallbackAction
    ) {
        String actor = safe(actorName).isBlank() ? "A user" : actorName.trim();
        String request = safe(requestName).isBlank() ? "your service request" : requestName.trim();

        String message;
        String action;
        NotificationPriority priority;

        switch (safe(type).toUpperCase(Locale.ROOT)) {
            case "NEW_SERVICE_REQUEST" -> {
                message = actor + " published a new service request: " + request + ".";
                priority = NotificationPriority.LOW;
                action = "Open request details";
            }
            case "NEW_APPLICATION" -> {
                message = actor + " submitted a new application for " + request + ".";
                priority = NotificationPriority.MEDIUM;
                action = "Review application";
            }
            case "APPLICATION_ACCEPTED" -> {
                message = "Your application for " + request + " was accepted. Great news!";
                priority = NotificationPriority.HIGH;
                action = "Prepare next steps";
            }
            case "APPLICATION_REJECTED" -> {
                message = "Your application for " + request + " was not selected this time.";
                priority = NotificationPriority.HIGH;
                action = "Improve profile and apply again";
            }
            default -> {
                message = fallbackMessage;
                priority = fallbackPriority;
                action = fallbackAction;
            }
        }

        if (safe(message).isBlank()) {
            message = fallbackMessage;
        }
        if (safe(action).isBlank()) {
            action = fallbackAction;
        }

        return new NotificationPayload(message, priority, action, false);
    }

    private NotificationPriority parsePriority(String raw, NotificationPriority fallback) {
        try {
            return NotificationPriority.valueOf(raw);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String extractJson(String content) {
        String trimmed = safe(content).trim();
        int first = trimmed.indexOf('{');
        int last = trimmed.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return trimmed.substring(first, last + 1);
        }
        return trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record NotificationPayload(
            String message,
            NotificationPriority priority,
            String suggestedAction,
            boolean generatedByAi
    ) {
    }
}
