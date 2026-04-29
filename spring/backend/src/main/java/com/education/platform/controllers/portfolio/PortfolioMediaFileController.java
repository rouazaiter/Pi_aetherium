package com.education.platform.controllers.portfolio;

import com.education.platform.services.implementations.portfolio.PortfolioMediaStorage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files/portfolio-media")
public class PortfolioMediaFileController {

    private final PortfolioMediaStorage portfolioMediaStorage;

    public PortfolioMediaFileController(PortfolioMediaStorage portfolioMediaStorage) {
        this.portfolioMediaStorage = portfolioMediaStorage;
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String filename) {
        Path path = portfolioMediaStorage.resolveExistingFile(filename);
        if (path == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path);
        String probe;
        try {
            probe = Files.probeContentType(path);
        } catch (Exception e) {
            probe = null;
        }
        MediaType mediaType =
                MediaType.parseMediaType(probe != null ? probe : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(mediaType)
                .body(resource);
    }
}
