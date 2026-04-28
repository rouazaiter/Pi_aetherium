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

import com.education.platform.entities.QuizResult;
import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeFile(String content) {

        try {
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

    public String generateQuiz(String transcript) {
        try {
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String systemPrompt = "Tu es un ingénieur pédagogique expert en création de tests.\n" +
                    "Basé sur la transcription d'un cours vidéo ci-dessous, génère 10 questions de type QCM (Choix multiples).\n" +
                    "Chaque question doit avoir 4 options (A, B, C, D) et une seule bonne réponse.\n" +
                    "RETOURNE UNIQUEMENT UN TABLEAU JSON au format suivant :\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"question\": \"Le texte de la question\",\n" +
                    "    \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
                    "    \"answer\": \"Le texte exact de la bonne réponse\",\n" +
                    "    \"explanation\": \"Une courte explication de pourquoi c'est la bonne réponse.\"\n" +
                    "  }\n" +
                    "]\n\n" +
                    "Transcription :\n" + transcript;

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", systemPrompt
            );

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(message),
                    "temperature", 0.5
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
            Map msg = (Map) choice.get("message");

            return cleanJson(msg.get("content").toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    private String cleanJson(String raw) {
        if (raw == null) return "";
        String cleaned = raw.trim();
        if (cleaned.contains("```json")) {
            int start = cleaned.indexOf("```json") + 7;
            int end = cleaned.lastIndexOf("```");
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
            }
        } else if (cleaned.contains("```")) {
            int start = cleaned.indexOf("```") + 3;
            int end = cleaned.lastIndexOf("```");
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
            }
        }
        return cleaned;
    }
    public String analyzeUserPerformance(List<QuizResult> results) {
        if (results.isEmpty()) return "Vous n'avez pas encore effectué de quizz. Commencez à apprendre pour voir votre analyse !";

        StringBuilder history = new StringBuilder();
        for (QuizResult res : results) {
            history.append("- Session: ").append(res.getFileName())
                   .append(" | Score: ").append(res.getScore()).append("/").append(res.getTotalQuestions())
                   .append("\n");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "Agis en tant que coach pédagogique. Analyse l'historique de résultats suivant d'un étudiant.\n" +
                    "Donne une analyse en 3 parties :\n" +
                    "1. Points Forts (ce qu'il maîtrise)\n" +
                    "2. Lacunes (ce qu'il doit travailler)\n" +
                    "3. Plan d'action (conseils concrets)\n\n" +
                    "Reste encourageant et concis. Réponds en FRANÇAIS.\n\n" +
                    "HISTORIQUE :\n" + history.toString();

            Map<String, Object> message = Map.of("role", "user", "content", prompt);
            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(message),
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/chat/completions", entity, Map.class);

            Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
            Map msg = (Map) choice.get("message");
            return msg.get("content").toString();

        } catch (Exception e) {
            return "Analyse indisponible pour le moment.";
        }
    }
}