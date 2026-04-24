package tn.esprit.backend.services.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Store the file on disk and return the stored file name (without path).
     */
    String store(MultipartFile file);

    /**
     * Load the file stored on disk.
     */
    Resource load(String storedFileName);

    /**
     * Infer the MIME type from the file extension.
     */
    MediaType getContentType(String storedFileName);
}

