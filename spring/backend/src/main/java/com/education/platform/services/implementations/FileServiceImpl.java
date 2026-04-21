package com.education.platform.services.implementations;

import com.education.platform.entities.AiJob;
import com.education.platform.entities.File;
import com.education.platform.entities.PrivateDrive;
import com.education.platform.repositories.AiJobRepository;
import com.education.platform.repositories.FileRepository;
import com.education.platform.repositories.PrivateDriveRepository;
import com.education.platform.services.interfaces.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
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
    @Autowired
    private AiService aiService;

    @Autowired
    private AiJobRepository jobRepository;
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
        return processVideo(fileId);
    }


    private String extractAudio(String videoPath) {

        String audioPath = videoPath + ".mp3";

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-q:a", "0",
                    "-map", "a",
                    audioPath
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            return audioPath;

        } catch (Exception e) {
            throw new RuntimeException("Audio extraction failed");
        }
    }


    @Override
    public String processVideo(Long fileId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // 👉 ici on simule contenu fichier
        String content = "File name: " + file.getName()
                + ". Type: " + file.getType()
                + ". This file belongs to a learning platform project.";

        return aiService.analyzeFile(content);
    }





    @Override
    public Long startAiSummary(Long fileId) {

        AiJob job = new AiJob();
        job.setFileId(fileId);
        job.setStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());

        job = jobRepository.save(job);

        // 🔥 lancer traitement async
        processAsync(job.getId(), fileId);

        return job.getId();
    }

    @Async
    public void processAsync(Long jobId, Long fileId) {

        AiJob job = jobRepository.findById(jobId).get();
        job.setStatus("PROCESSING");
        jobRepository.save(job);

        try {
            String result = processVideo(fileId); // ton code existant

            job.setResult(result);
            job.setStatus("DONE");

        } catch (Exception e) {
            job.setStatus("ERROR");
            job.setResult("Failed: " + e.getMessage());
        }

        jobRepository.save(job);
    }

    @Override
    public List<File> searchFiles(Long userId, String keyword) {

        System.out.println("USER ID = " + userId);
        System.out.println("KEYWORD = " + keyword);

        PrivateDrive drive = driveRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Drive not found"));

        List<File> result = fileRepository.findByDrive_IdAndNameContainingIgnoreCase(
                drive.getId(), keyword
        );

        System.out.println("RESULT SIZE = " + result.size());

        return result;
    }

}