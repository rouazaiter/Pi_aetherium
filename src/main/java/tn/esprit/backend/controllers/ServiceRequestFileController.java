package tn.esprit.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.backend.services.interfaces.FileStorageService;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestFileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/files/{storedFileName}")
    public ResponseEntity<Resource> download(@PathVariable String storedFileName) {
        Resource resource = fileStorageService.load(storedFileName);
        return ResponseEntity.ok()
                .contentType(fileStorageService.getContentType(storedFileName))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + storedFileName + "\"")
                .body(resource);
    }
}

