package com.education.platform.services.implementations;

import com.education.platform.entities.File;
import com.education.platform.entities.PrivateDrive;
import com.education.platform.repositories.FileRepository;
import com.education.platform.repositories.PrivateDriveRepository;
import com.education.platform.services.interfaces.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final String STORAGE_PATH = "D:/storage-users-pi/";

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private PrivateDriveRepository driveRepository;

    // ========================= UPLOAD =========================
    @Override
    public File uploadFile(MultipartFile file, Long userId) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        PrivateDrive drive = driveRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Drive not found"));

        // 🚨 QUOTA CHECK
        if (drive.getUsedVolume() + file.getSize() > drive.getTotalVolume()) {
            throw new RuntimeException("Quota exceeded");
        }

        // 📁 CREATE USER FOLDER
        String userFolder = STORAGE_PATH + "user-" + userId + "/";
        try {
            Files.createDirectories(Paths.get(userFolder));
        } catch (IOException e) {
            throw new RuntimeException("Cannot create user folder");
        }

        // 🔒 SAFE UNIQUE FILE NAME
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = userFolder + fileName;

        // 💾 SAVE FILE ON DISK
        try {
            Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed");
        }

        // 💾 SAVE DB ENTITY
        File f = new File();
        f.setName(file.getOriginalFilename());
        f.setPath(filePath);
        f.setType(file.getContentType());
        f.setSize(file.getSize());
        f.setCreatedAt(LocalDateTime.now());
        f.setDrive(drive);

        // 📊 UPDATE QUOTA
        drive.setUsedVolume(drive.getUsedVolume() + file.getSize());
        driveRepository.save(drive);

        return fileRepository.save(f);
    }

    // ========================= LIST FILES =========================
    @Override
    public List<File> getUserFiles(Long userId) {

        PrivateDrive drive = driveRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Drive not found"));

        return fileRepository.findByDriveId(drive.getId());
    }

    // ========================= DELETE =========================
    @Override
    public void deleteFile(Long fileId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        PrivateDrive drive = file.getDrive();

        // 🗑 DELETE FROM DISK
        try {
            Files.deleteIfExists(Paths.get(file.getPath()));
        } catch (IOException e) {
            throw new RuntimeException("Delete failed");
        }

        // 📉 UPDATE QUOTA
        drive.setUsedVolume(drive.getUsedVolume() - file.getSize());
        driveRepository.save(drive);

        fileRepository.delete(file);
    }

    // ========================= RENAME =========================
    @Override
    public File renameFile(Long fileId, String newName) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        file.setName(newName);

        return fileRepository.save(file);
    }

    // ========================= DOWNLOAD =========================
    @Override
    public Resource downloadFile(Long fileId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return new FileSystemResource(file.getPath());
    }


    // ========================= SUMMARIZE =========================
    @Override
    public String summarizeFile(Long fileId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return "Résumé automatique de la vidéo : introduction, concepts clés, conclusion...";
    }
}