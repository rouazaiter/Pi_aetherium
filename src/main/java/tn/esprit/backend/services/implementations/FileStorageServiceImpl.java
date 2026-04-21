package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.services.interfaces.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "png", "jpg", "jpeg"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg"
    );

    @Value("${app.upload.service-requests-dir}")
    private String serviceRequestsUploadDir;

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Empty file");
        }

        String originalName = file.getOriginalFilename();
        String ext = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Unauthorized extension: " + ext);
        }

        // Lightweight server-side check: content-type can be spoofed,
        // but we keep it as an extra safeguard.
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Unauthorized content type: " + contentType);
        }

        String storedFileName = UUID.randomUUID() + "." + ext;
        Path uploadDir = getUploadDir();
        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(storedFileName);
            file.transferTo(target);
        } catch (IOException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while storing file", e);
        }

        return storedFileName;
    }

    @Override
    public Resource load(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing file name");
        }

        String ext = extractExtension(storedFileName);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Unauthorized extension: " + ext);
        }

        Path target = getUploadDir().resolve(storedFileName).normalize();
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "File not found");
        }

        return new FileSystemResource(target.toFile());
    }

    @Override
    public MediaType getContentType(String storedFileName) {
        String ext = extractExtension(storedFileName);
        return switch (ext) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "doc" -> MediaType.valueOf("application/msword");
            case "docx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg" -> MediaType.valueOf("image/jpeg");
            case "jpeg" -> MediaType.valueOf("image/jpeg");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private Path getUploadDir() {
        Path p = Paths.get(serviceRequestsUploadDir);
        return p.toAbsolutePath().normalize();
    }

    private String extractExtension(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

