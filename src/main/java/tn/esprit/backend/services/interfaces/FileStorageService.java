package tn.esprit.backend.services.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Stocke le fichier sur disque et retourne le nom de fichier stocké (sans chemin).
     */
    String store(MultipartFile file);

    /**
     * Charge le fichier stocké sur disque.
     */
    Resource load(String storedFileName);

    /**
     * Déduit le type MIME à partir de l'extension.
     */
    MediaType getContentType(String storedFileName);
}

