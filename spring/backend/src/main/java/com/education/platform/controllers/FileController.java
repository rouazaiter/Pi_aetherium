package com.education.platform.controllers;

import com.education.platform.entities.AiJob;
import com.education.platform.entities.File;
import com.education.platform.repositories.AiJobRepository;
import com.education.platform.repositories.FileRepository;
import com.education.platform.services.interfaces.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private AiJobRepository aiJobRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private com.education.platform.services.implementations.AiService aiService;

    @PostMapping("/upload")
    public File upload(@RequestParam("file") MultipartFile file,
                       @RequestParam Long userId) {
        return fileService.uploadFile(file, userId);
    }

    @GetMapping
    public List<File> getFiles(@RequestParam Long userId) {
        return fileService.getUserFiles(userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        fileService.deleteFile(id);
    }

    @PutMapping("/{id}")
    public File rename(@PathVariable Long id,
                       @RequestParam String name) {
        return fileService.renameFile(id, name);
    }

    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<Resource> thumbnail(@PathVariable Long id) {
        try {
            File fileEntity = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            if (fileEntity.getThumbnailPath() == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(fileEntity.getThumbnailPath());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // cache 24h côté browser
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        try {
            File fileEntity = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            Resource resource = new FileSystemResource(fileEntity.getPath());

            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String filename = fileEntity.getName() != null ? fileEntity.getName() : resource.getFilename();
            String contentType = fileEntity.getType() != null ? fileEntity.getType() : "application/octet-stream";

            String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> stream(@PathVariable Long id) throws IOException {

        Resource resource = fileService.downloadFile(id);

        String contentType = Files.probeContentType(resource.getFile().toPath());
        if (contentType == null) contentType = "video/mp4";

        // Return 200 OK — Spring Boot handles Range requests automatically for FileSystemResource
        // Accept-Ranges: bytes tells the browser it can seek/buffer the video
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }


    @PostMapping("/chat/{id}")
    public ResponseEntity<Map<String, String>> chatWithVideo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            File fileEntity = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            String question = body.get("question");
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
            }

            String transcriptPath = fileEntity.getPath() + "_transcript.txt";
            Path path = Paths.get(transcriptPath);
            
            if (!Files.exists(path)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Veuillez d'abord générer le résumé IA pour activer le Tuteur IA."));
            }

            String transcript = Files.readString(path);
            String answer = aiService.chatWithVideoContext(transcript, question);

            return ResponseEntity.ok(Map.of("answer", answer));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Chat failed: " + e.getMessage()));
        }
    }

    @GetMapping("/summary/{id}")
    public ResponseEntity<Resource> getSummary(@PathVariable Long id) {
        try {
            File fileEntity = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            String summaryPath = fileEntity.getPath() + "_summary.txt";
            Path path = Paths.get(summaryPath);

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path.toFile());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=summary.txt")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        try {
            File fileEntity = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            String reportPath = fileEntity.getPath() + "_report.json";
            Path path = Paths.get(reportPath);

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            String jsonReport = Files.readString(path);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(jsonReport);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Report failed: " + e.getMessage()));
        }
    }

    @PostMapping("/global-chat")
    public ResponseEntity<Map<String, String>> globalDriveChat(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String question = body.get("question").toString();

            List<File> userFiles = fileService.getUserFiles(userId);
            StringBuilder aggregatedContext = new StringBuilder("[\n");

            for (File f : userFiles) {
                String reportPath = f.getPath() + "_report.json";
                Path path = Paths.get(reportPath);
                if (Files.exists(path)) {
                    String jsonReport = Files.readString(path);
                    aggregatedContext.append("{ \"fileName\": \"").append(f.getName()).append("\", ");
                    aggregatedContext.append("\"reportData\": ").append(jsonReport).append(" },\n");
                }
            }
            aggregatedContext.append("{}]"); // Close JSON array properly

            if (aggregatedContext.length() < 10) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun bilan de session disponible dans votre Drive. Générez d'abord des bilans."));
            }

            String answer = aiService.globalDriveChat(aggregatedContext.toString(), question);
            return ResponseEntity.ok(Map.of("answer", answer));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Global Chat failed: " + e.getMessage()));
        }
    }


    @PostMapping("/upload-session")
    public File uploadSession(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId
    ) {
        return fileService.uploadFile(file, userId);
    }


    //resumé d'upload :
    @PostMapping("/summarize/{fileId}")
    public ResponseEntity<Map<String, String>> summarize(@PathVariable Long fileId) {

        String result = fileService.processVideo(fileId);

        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/ai-summary/{fileId}")
    public Long startSummary(@PathVariable Long fileId) {
        return fileService.startAiSummary(fileId);
    }

    @GetMapping("/ai-summary/status/{jobId}")
    public AiJob getStatus(@PathVariable Long jobId) {
        return aiJobRepository.findById(jobId).orElse(null);
    }


    @GetMapping("/search")
    public List<File> searchFiles(
            @RequestParam Long userId,
            @RequestParam String keyword
    ) {
        return fileService.searchFiles(userId, keyword);
    }




    // ========================= TRANSCRIPTION =========================
    @PostMapping("/transcribe/{fileId}")
    public Long startTranscription(@PathVariable Long fileId) {
        return fileService.startTranscription(fileId);
    }

    @GetMapping("/transcribe/status/{jobId}")
    public AiJob getTranscriptionStatus(@PathVariable Long jobId) {
        return aiJobRepository.findById(jobId).orElse(null);
    }

//    @GetMapping("/transcript/{jobId}")
//    public ResponseEntity<Resource> downloadTranscript(@PathVariable Long jobId) {
//
//        AiJob job = aiJobRepository.findById(jobId)
//                .orElseThrow(() -> new RuntimeException("Job not found"));
//
//        java.nio.file.Path path = java.nio.file.Paths.get(job.getResult());
//
//        Resource resource = new FileSystemResource(path.toFile());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transcript.txt")
//                .body(resource);
//    }

    @GetMapping("/transcript/{jobId}")
    public ResponseEntity<Resource> downloadTranscript(@PathVariable Long jobId) {

        AiJob job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        FileSystemResource file = new FileSystemResource(job.getResult());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=summary.txt")
                .body(file);
    }

//    @GetMapping("/summary-file")
//    public ResponseEntity<String> getSummary(@RequestParam String path) throws IOException {
//
//        Path file = Paths.get(path);
//
//        String content = Files.readString(file);
//
//        return ResponseEntity.ok(content);
//    }

    @GetMapping("/summary-file")
    public ResponseEntity<String> readSummary(@RequestParam String path) throws IOException {
        return ResponseEntity.ok(Files.readString(Path.of(path)));
    }



}
