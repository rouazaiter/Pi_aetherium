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
public class ProfilePictureStorage {

    private static final long MAX_BYTES = 3 * 1024 * 1024;
    private static final Pattern STORED_NAME =
            Pattern.compile("[0-9]+_[a-f0-9-]+\\.(jpg|jpeg|png|gif|webp)", Pattern.CASE_INSENSITIVE);

    private final UploadProperties uploadProperties;

    public ProfilePictureStorage(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    /**
     * Enregistre le fichier et retourne le chemin relatif API, ex. {@code /api/files/profile-pictures/1_uuid.jpg}.
     */
    public String store(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Fichier vide.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image trop volumineuse (maximum 3 Mo).");
        }
        String ext = extensionForContentType(file.getContentType());
        if (ext == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Format d’image non pris en charge (JPEG, PNG, WebP, GIF).");
        }

        Path dir = uploadDir();
        Files.createDirectories(dir);

        String name = userId + "_" + UUID.randomUUID() + ext;
        Path target = dir.resolve(name).normalize();
        if (!target.startsWith(dir)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide.");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/api/files/profile-pictures/" + name;
    }

    /** Supprime un fichier précédemment servi par cette appli (évite les URLs externes). */
    public void deleteIfManagedByUs(String profilePictureValue) {
        if (profilePictureValue == null || profilePictureValue.isBlank()) {
            return;
        }
        String prefix = "/api/files/profile-pictures/";
        if (!profilePictureValue.startsWith(prefix)) {
            return;
        }
        String name = profilePictureValue.substring(prefix.length());
        if (!STORED_NAME.matcher(name).matches()) {
            return;
        }
        Path dir = uploadDir();
        Path target = dir.resolve(name).normalize();
        if (!target.startsWith(dir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // ne bloque pas la mise à jour du profil
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
        return Path.of(uploadProperties.getProfilePicturesDir()).toAbsolutePath().normalize();
    }

    private static String extensionForContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        String ct = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
        return switch (ct) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> null;
        };
    }
}
