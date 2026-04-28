package com.education.platform.services.interfaces;
import com.education.platform.entities.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    File uploadFile(MultipartFile file, Long userId);

    List<File> getUserFiles(Long userId);

    void deleteFile(Long fileId);

    File renameFile(Long fileId, String newName);

    Resource downloadFile(Long fileId);

    // ========================= SUMMARIZE =========================
    String summarizeFile(Long fileId);

    String processVideo(Long fileId);


    Long startAiSummary(Long fileId);

    List<File> searchFiles(Long userId, String keyword);

    // ========================= TRANSCRIPTION =========================
    Long startTranscription(Long fileId);


}
