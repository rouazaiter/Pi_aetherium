package tn.esprit.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recordings")
public class RecordingController {

    @Value("${app.recordings.directory:./recordings}")
    private String recordingsDirectory;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadRecording(
            @RequestParam("video") MultipartFile video,
            @RequestParam("sessionId") Long sessionId,
            @RequestParam(value = "fileName", required = false) String fileName) {

        if (sessionId == null || sessionId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid sessionId"));
        }

        if (video == null || video.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or empty video file"));
        }

        String contentType = video.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().contains("video")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid content type. Expected a video file."));
        }

        try {
            Path directoryPath = Paths.get(recordingsDirectory).toAbsolutePath().normalize();
            Files.createDirectories(directoryPath);

            if (fileName == null || fileName.isBlank()) {
                fileName = sessionId + ".webm";
            }
            if (!fileName.endsWith(".webm")) {
                fileName += ".webm";
            }
            Path targetPath = directoryPath.resolve(fileName).normalize();

            Files.copy(video.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("fileName", fileName);
            response.put("path", targetPath.toString());
            response.put("size", video.getSize());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save recording file"));
        }
    }
}

