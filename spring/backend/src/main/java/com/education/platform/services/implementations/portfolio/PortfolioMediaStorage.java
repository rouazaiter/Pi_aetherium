package com.education.platform.services.implementations.portfolio;

import com.education.platform.common.ApiException;
import com.education.platform.config.UploadProperties;
import com.education.platform.dto.portfolio.UploadedProjectMediaResponse;
import com.education.platform.entities.portfolio.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PortfolioMediaStorage {

    private static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 50 * 1024 * 1024;
    private static final Pattern STORED_NAME =
            Pattern.compile("[0-9]+_[a-f0-9-]+\\.(jpg|jpeg|png|gif|webp|mp4|webm|mov|ogg)", Pattern.CASE_INSENSITIVE);
    private static final String API_PREFIX = "/api/files/portfolio-media/";

    private final UploadProperties uploadProperties;

    public PortfolioMediaStorage(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public UploadedProjectMediaResponse store(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Fichier vide.");
        }

        StoredMediaDescriptor descriptor = descriptorForContentType(file.getContentType());
        if (descriptor == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Format non pris en charge. Images: JPEG, PNG, WebP, GIF. Videos: MP4, WebM, MOV, OGG.");
        }
        if (file.getSize() > descriptor.maxBytes()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, descriptor.mediaType() == MediaType.IMAGE
                    ? "Image trop volumineuse (maximum 10 Mo)."
                    : "Video trop volumineuse (maximum 50 Mo).");
        }

        Path dir = uploadDir();
        Files.createDirectories(dir);

        String name = userId + "_" + UUID.randomUUID() + descriptor.extension();
        Path target = dir.resolve(name).normalize();
        if (!target.startsWith(dir)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide.");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return UploadedProjectMediaResponse.builder()
                .mediaUrl(API_PREFIX + name)
                .mediaType(descriptor.mediaType())
                .filename(name)
                .build();
    }

    public void deleteIfManagedByUs(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank() || !mediaUrl.startsWith(API_PREFIX)) {
            return;
        }
        String filename = mediaUrl.substring(API_PREFIX.length());
        if (!STORED_NAME.matcher(filename).matches()) {
            return;
        }
        Path dir = uploadDir();
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // avoid blocking business updates on file cleanup
        }
    }

    public Path resolveExistingFile(String filename) {
        if (!STORED_NAME.matcher(filename).matches()) {
            return null;
        }
        Path dir = uploadDir();
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir) || !Files.isRegularFile(target)) {
            return null;
        }
        return target;
    }

    private Path uploadDir() {
        return Path.of(uploadProperties.getPortfolioMediaDir()).toAbsolutePath().normalize();
    }

    private static StoredMediaDescriptor descriptorForContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        String ct = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
        return switch (ct) {
            case "image/jpeg", "image/jpg" -> new StoredMediaDescriptor(MediaType.IMAGE, ".jpg", MAX_IMAGE_BYTES);
            case "image/png" -> new StoredMediaDescriptor(MediaType.IMAGE, ".png", MAX_IMAGE_BYTES);
            case "image/webp" -> new StoredMediaDescriptor(MediaType.IMAGE, ".webp", MAX_IMAGE_BYTES);
            case "image/gif" -> new StoredMediaDescriptor(MediaType.IMAGE, ".gif", MAX_IMAGE_BYTES);
            case "video/mp4" -> new StoredMediaDescriptor(MediaType.VIDEO, ".mp4", MAX_VIDEO_BYTES);
            case "video/webm" -> new StoredMediaDescriptor(MediaType.VIDEO, ".webm", MAX_VIDEO_BYTES);
            case "video/quicktime" -> new StoredMediaDescriptor(MediaType.VIDEO, ".mov", MAX_VIDEO_BYTES);
            case "video/ogg" -> new StoredMediaDescriptor(MediaType.VIDEO, ".ogg", MAX_VIDEO_BYTES);
            default -> null;
        };
    }

    private record StoredMediaDescriptor(MediaType mediaType, String extension, long maxBytes) {
    }
}
