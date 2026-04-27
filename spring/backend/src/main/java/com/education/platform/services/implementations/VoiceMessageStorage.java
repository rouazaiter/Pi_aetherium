package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.config.UploadProperties;
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
public class VoiceMessageStorage {

    private static final long MAX_BYTES = 10 * 1024 * 1024;
    private static final Pattern STORED_NAME =
            Pattern.compile("[0-9]+_[a-f0-9-]+\\.(webm|ogg|wav|m4a|mp3)", Pattern.CASE_INSENSITIVE);

    private final UploadProperties uploadProperties;

    public VoiceMessageStorage(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public String store(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Audio file is empty.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Audio too large (maximum 10 MB).");
        }
        String ext = extensionForContentType(file.getContentType(), file.getOriginalFilename());
        if (ext == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported audio format.");
        }

        Path dir = uploadDir();
        Files.createDirectories(dir);

        String name = userId + "_" + UUID.randomUUID() + ext;
        Path target = dir.resolve(name).normalize();
        if (!target.startsWith(dir)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file name.");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/api/files/voice-messages/" + name;
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
        return Path.of(uploadProperties.getVoiceMessagesDir()).toAbsolutePath().normalize();
    }

    private static String extensionForContentType(String contentType, String originalFilename) {
        if (contentType != null) {
            String ct = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
            return switch (ct) {
                case "audio/webm" -> ".webm";
                case "audio/ogg" -> ".ogg";
                case "audio/wav", "audio/x-wav" -> ".wav";
                case "audio/mp4", "audio/x-m4a" -> ".m4a";
                case "audio/mpeg", "audio/mp3" -> ".mp3";
                default -> null;
            };
        }
        if (originalFilename == null) {
            return null;
        }
        String lower = originalFilename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".webm")) return ".webm";
        if (lower.endsWith(".ogg")) return ".ogg";
        if (lower.endsWith(".wav")) return ".wav";
        if (lower.endsWith(".m4a")) return ".m4a";
        if (lower.endsWith(".mp3")) return ".mp3";
        return null;
    }
}
