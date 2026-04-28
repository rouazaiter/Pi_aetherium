package com.education.platform.services.implementations;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;

@Service
public class VideoService {

    public long getVideoDurationSeconds(String filePath) {
        try {

            // ✅ NORMALISATION WINDOWS
            filePath = new java.io.File(filePath).getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    filePath
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes()).trim();

            System.out.println("FFPROBE OUTPUT = [" + output + "]");

            if (output.isBlank()) {
                return 0;
            }

            double seconds = Double.parseDouble(output);

            return Math.round(seconds);

        } catch (Exception e) {
            System.out.println("FFPROBE ERROR = " + e.getMessage());
            return 0;
        }
    }

    /**
     * Génère une miniature JPG à 10% de la durée de la vidéo.
     * Utilise FFmpeg (déjà installé via ffprobe).
     *
     * @param videoPath chemin absolu de la vidéo
     * @param duration  durée en secondes (déjà calculée)
     * @return chemin du fichier thumbnail généré, ou null en cas d'échec
     */
    public String generateThumbnail(String videoPath, long duration) {
        try {
            videoPath = new java.io.File(videoPath).getAbsolutePath();

            // Position à 10% de la durée (minimum 1 seconde)
            long seekSeconds = Math.max(1, duration / 10);
            String timestamp = formatTimestamp(seekSeconds);

            String thumbnailPath = videoPath + "_thumb.jpg";

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",                        // overwrite si existe
                    "-ss", timestamp,            // seek AVANT le décodage (rapide)
                    "-i", videoPath,
                    "-vframes", "1",             // 1 seule frame
                    "-q:v", "2",                 // qualité (2 = haute, 31 = basse)
                    "-vf", "scale=480:-1",       // largeur 480px, hauteur proportionnelle
                    thumbnailPath
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Lire output pour éviter blocage du buffer
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            System.out.println("FFMPEG THUMBNAIL exit=" + exitCode + " path=" + thumbnailPath);

            if (exitCode == 0) {
                return thumbnailPath;
            } else {
                System.out.println("FFMPEG THUMBNAIL FAILED: " + output);
                return null;
            }

        } catch (Exception e) {
            System.out.println("FFMPEG THUMBNAIL ERROR = " + e.getMessage());
            return null;
        }
    }

    private String formatTimestamp(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private String formatTime(long seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", min, sec);
    }
}