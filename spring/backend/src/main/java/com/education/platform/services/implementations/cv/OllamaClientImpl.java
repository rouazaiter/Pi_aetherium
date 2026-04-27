package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.config.OllamaAiProperties;
import com.education.platform.services.interfaces.cv.OllamaClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class OllamaClientImpl implements OllamaClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OllamaAiProperties ollamaAiProperties;

    public OllamaClientImpl(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            OllamaAiProperties ollamaAiProperties) {
        this.restClient = restClientBuilder
                .baseUrl(trimTrailingSlash(ollamaAiProperties.getBaseUrl()))
                .build();
        this.objectMapper = objectMapper;
        this.ollamaAiProperties = ollamaAiProperties;
    }

    @Override
    public String generate(String prompt) {
        try {
            String responseBody = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", ollamaAiProperties.getModel(),
                            "prompt", prompt,
                            "stream", false
                    ))
                    .retrieve()
                    .body(String.class);

            return extractResponse(responseBody);
        } catch (ApiException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Ollama is unavailable");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate AI suggestion");
        }
    }

    private String extractResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String response = root.path("response").asText(null);
            if (response == null) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Ollama response");
            }
            return response;
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Ollama response");
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
