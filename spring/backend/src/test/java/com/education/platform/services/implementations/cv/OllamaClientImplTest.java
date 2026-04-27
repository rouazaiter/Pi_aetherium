package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.config.OllamaAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaClientImplTest {

    @Test
    void generateReturnsResponseFieldFromOllamaPayload() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OllamaAiProperties properties = new OllamaAiProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("qwen3:8b");
        OllamaClientImpl client = new OllamaClientImpl(builder, new ObjectMapper(), properties);

        server.expect(requestTo("http://localhost:11434/api/generate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"response\":\"Improved text\"}", MediaType.APPLICATION_JSON));

        String result = client.generate("prompt");

        assertEquals("Improved text", result);
        server.verify();
    }

    @Test
    void generateMapsOllamaErrorsToServiceUnavailable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OllamaAiProperties properties = new OllamaAiProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("qwen3:8b");
        OllamaClientImpl client = new OllamaClientImpl(builder, new ObjectMapper(), properties);

        server.expect(requestTo("http://localhost:11434/api/generate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        ApiException exception = assertThrows(ApiException.class, () -> client.generate("prompt"));

        assertEquals(503, exception.getStatus().value());
        assertEquals("Ollama is unavailable", exception.getMessage());
    }
}
