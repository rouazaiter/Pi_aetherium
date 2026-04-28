package com.education.platform.services.implementations;

import com.education.platform.entities.JobStatus;
import com.education.platform.entities.AiJob;
import com.education.platform.entities.File;
import com.education.platform.repositories.AiJobRepository;
import com.education.platform.repositories.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class AsyncJobService {

    @Autowired
    private AiJobRepository jobRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private VideoService videoService;

    // ======================= SUMMARY SIMPLE =======================
    @Async
    public void processAiSummaryAsync(Long jobId, Long fileId) {

        AiJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);

        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            String content = "File name: " + file.getName()
                    + ". Type: " + file.getType();

            String result = aiService.analyzeFile(content);

            job.setResult(result);
            job.setStatus(JobStatus.DONE);

        } catch (Exception e) {
            job.setStatus(JobStatus.ERROR);
            job.setResult("Failed: " + e.getMessage());
        }

        jobRepository.save(job);
    }

    // ======================= TRANSCRIPTION =======================
    @Async
    public void transcribeAsync(Long jobId, Long fileId) {

        AiJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);

        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // ===================== DURATION SAFE =====================
            long duration = file.getDuration();

            if (duration <= 0) {
                duration = videoService.getVideoDurationSeconds(file.getPath());
                file.setDuration(duration);
                fileRepository.save(file);
            }

            System.out.println("FINAL DURATION = " + duration);

            // ===================== CALL PYTHON =====================
            var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(600000);
            factory.setReadTimeout(600000);

            RestTemplate restTemplate = new RestTemplate(factory);

            Map<String, String> body = Map.of("file_path", file.getPath());

            Map response = restTemplate.postForObject(
                    "http://localhost:8000/transcribe",
                    body,
                    Map.class
            );

            String transcript = (String) response.get("text");

            // ===================== CHUNKING =====================
            int chunkSize = 2500;
            StringBuilder combinedSummary = new StringBuilder();

            for (int i = 0; i < transcript.length(); i += chunkSize) {

                int end = Math.min(i + chunkSize, transcript.length());
                String chunk = transcript.substring(i, end);

                String prompt =
                        "Analyse cette transcription:\n\n"
                                + chunk;

                combinedSummary.append(aiService.analyzeFile(prompt))
                        .append("\n\n");
            }

            // ===================== FINAL PROMPT =====================
            String globalPrompt =
                    "Tu es un expert en analyse vidéo.\n\n"
                            + "Durée de la vidéo: " + duration + " secondes.\n\n"
                            + "Rédige un rapport structuré en suivant STRICTEMENT cet ordre et ce format :\n\n"
                            + "1. TIMELINE\n"
                            + "Format: 0:00 - X:XX → Titre\n"
                            + "Résumé: (court résumé de la section)\n"
                            + "(Génère 3 à 6 sections maximum)\n\n"
                            + "2. KEYWORDS\n"
                            + "Liste 5 à 10 mots-clés importants commençant par un tiret (-).\n\n"
                            + "3. USAGE\n"
                            + "Liste 3 à 5 conseils d'utilisation commençant par un numéro (1., 2., ...).\n\n"
                            + "CONTENU A ANALYSER :\n"
                            + combinedSummary;

            String finalResult = aiService.analyzeFile(globalPrompt);

            // ===================== SAVE =====================
            String summaryPath = file.getPath() + "_summary.txt";
            Files.writeString(Paths.get(summaryPath), finalResult);

            // 💾 Sauvegarde du transcript brut pour le Tuteur IA (RAG)
            String rawTranscriptPath = file.getPath() + "_transcript.txt";
            Files.writeString(Paths.get(rawTranscriptPath), transcript);

            // ===================== JSON REPORT (BILAN DE SESSION) =====================
            String reportPrompt = "Analyse cette session de mentorat/consulting.\n\n"
                    + "Tu dois extraire les informations clés et répondre UNIQUEMENT au format JSON strict, sans texte avant ni après, sans balises ```json.\n\n"
                    + "Format attendu:\n"
                    + "{\n"
                    + "  \"problem\": \"Résumé en 1 phrase du problème ou de l'objectif initial du client.\",\n"
                    + "  \"solution\": \"Résumé en 1-2 phrases de la solution ou du conseil apporté par l'expert.\",\n"
                    + "  \"actionItems\": [\"action 1 à faire\", \"action 2 à faire\"],\n"
                    + "  \"technologies\": [\"tech1\", \"tech2\"]\n"
                    + "}\n\n"
                    + "TRANSCRIPTION:\n" + transcript;

            String jsonReport = aiService.analyzeFile(reportPrompt);
            
            // Nettoyage robuste au cas où GPT ajoute des balises Markdown ou du texte avant/après
            if (jsonReport.contains("```json")) {
                int start = jsonReport.indexOf("```json") + 7;
                int end = jsonReport.lastIndexOf("```");
                if (end > start) {
                    jsonReport = jsonReport.substring(start, end).trim();
                }
            } else if (jsonReport.contains("```")) {
                int start = jsonReport.indexOf("```") + 3;
                int end = jsonReport.lastIndexOf("```");
                if (end > start) {
                    jsonReport = jsonReport.substring(start, end).trim();
                }
            }
            jsonReport = jsonReport.trim();

            String reportPath = file.getPath() + "_report.json";
            Files.writeString(Paths.get(reportPath), jsonReport);

            job.setResult(summaryPath);
            job.setStatus(JobStatus.DONE);

        } catch (Exception e) {
            job.setStatus(JobStatus.ERROR);
            job.setResult("Transcription failed: " + e.getMessage());
        }

        jobRepository.save(job);
    }

    // ======================= FORMAT TIME FIX =======================
    private String formatTime(long seconds) {

        long min = seconds / 60;
        long sec = seconds % 60;

        return String.format("%d:%02d", min, sec);
    }
}