package com.education.platform.controllers;

import com.education.platform.entities.AiJob;
import com.education.platform.entities.File;
import com.education.platform.repositories.AiJobRepository;
import com.education.platform.services.interfaces.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private AiJobRepository aiJobRepository;

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

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {

        Resource resource = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // 🔥 IMPORTANT
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4") // 🔥 important pour Angular
                .body(resource);
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

}
