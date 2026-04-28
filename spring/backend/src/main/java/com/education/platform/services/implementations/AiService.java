package com.education.platform.services.implementations;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.audio.CreateTranscriptionRequest;
import com.theokanning.openai.completion.CompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    public String analyzeFile(String content) {

        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", "Analyze this file and give: summary + keywords + usage suggestion:\n" + content
            );

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(message)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
            Map msg = (Map) choice.get("message");

            return msg.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI error: " + e.getMessage();
        }
    }

    public String chatWithVideoContext(String transcript, String question) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String systemPrompt = "Tu es un tuteur pédagogique expert. " +
                    "Voici la transcription exacte d'un cours vidéo que l'étudiant est en train de regarder :\n\n" +
                    "--- DEBUT TRANSCRIPTION ---\n" +
                    transcript + "\n" +
                    "--- FIN TRANSCRIPTION ---\n\n" +
                    "Réponds à la question de l'étudiant de manière claire, concise et pédagogique. " +
                    "Base-toi STRICTEMENT sur la transcription ci-dessus. Si la réponse n'y figure pas, dis-le poliment.";

            Map<String, Object> systemMsg = Map.of(
                    "role", "system",
                    "content", systemPrompt
            );

            Map<String, Object> userMsg = Map.of(
                    "role", "user",
                    "content", question
            );

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(systemMsg, userMsg),
                    "temperature", 0.3 // Faible température pour rester fidèle au cours
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
            Map msg = (Map) choice.get("message");

            return msg.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Désolé, je n'ai pas pu analyser la vidéo. Erreur: " + e.getMessage();
        }
    }

    public String globalDriveChat(String aggregatedContext, String question) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String systemPrompt = "Tu es 'Le Cerveau du Drive', l'assistant IA global de l'utilisateur.\n" +
                    "Voici une compilation au format JSON de tous les bilans de sessions (vidéos) stockés dans son Drive :\n\n" +
                    "--- DEBUT DONNEES DRIVE ---\n" +
                    aggregatedContext + "\n" +
                    "--- FIN DONNEES DRIVE ---\n\n" +
                    "Ton rôle est d'analyser ces données globales pour répondre à la question de l'utilisateur.\n" +
                    "Sois précis, cite le nom du fichier (vidéo) quand c'est pertinent. " +
                    "Utilise un formatage clair (Markdown, gras, listes). " +
                    "Si la réponse ne se trouve pas dans ces bilans, dis-le poliment.";

            Map<String, Object> systemMsg = Map.of(
                    "role", "system",
                    "content", systemPrompt
            );

            Map<String, Object> userMsg = Map.of(
                    "role", "user",
                    "content", question
            );

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(systemMsg, userMsg),
                    "temperature", 0.4
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
            Map msg = (Map) choice.get("message");

            return msg.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de l'Assistant Global: " + e.getMessage();
        }
    }
}