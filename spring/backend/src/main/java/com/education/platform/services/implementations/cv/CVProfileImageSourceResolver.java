package com.education.platform.services.implementations.cv;

import com.education.platform.services.implementations.ProfilePictureStorage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CVProfileImageSourceResolver {

    private static final Pattern PRESET_PATTERN = Pattern.compile("^skillhub-preset:([1-8])$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGACY_PRESET_PATTERN = Pattern.compile("^/avatars/preset-([1-8])\\.svg$", Pattern.CASE_INSENSITIVE);
    private static final Map<String, String> PRESET_SVGS = Map.of(
            "1", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"a\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop offset=\"0%\" stop-color=\"#7a6ad8\"/><stop offset=\"100%\" stop-color=\"#5b52c7\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#a)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".92\"/><ellipse cx=\"50\" cy=\"78\" rx=\"30\" ry=\"20\" fill=\"#fff\" opacity=\".88\"/></svg>",
            "2", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"b\" x1=\"0\" y1=\"1\" x2=\"1\" y2=\"0\"><stop offset=\"0%\" stop-color=\"#f97316\"/><stop offset=\"100%\" stop-color=\"#fb7185\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#b)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".9\"/><path d=\"M20 78 Q50 58 80 78 L80 88 Q50 72 20 88 Z\" fill=\"#fff\" opacity=\".85\"/></svg>",
            "3", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"c\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop offset=\"0%\" stop-color=\"#0ea5e9\"/><stop offset=\"100%\" stop-color=\"#6366f1\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#c)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".9\"/><ellipse cx=\"50\" cy=\"76\" rx=\"28\" ry=\"18\" fill=\"#fff\" opacity=\".82\"/></svg>",
            "4", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"d\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop offset=\"0%\" stop-color=\"#22c55e\"/><stop offset=\"100%\" stop-color=\"#059669\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#d)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".9\"/><ellipse cx=\"50\" cy=\"78\" rx=\"30\" ry=\"19\" fill=\"#fff\" opacity=\".85\"/></svg>",
            "5", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"e\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop offset=\"0%\" stop-color=\"#eab308\"/><stop offset=\"100%\" stop-color=\"#f59e0b\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#e)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".92\"/><ellipse cx=\"50\" cy=\"77\" rx=\"29\" ry=\"19\" fill=\"#fff\" opacity=\".86\"/></svg>",
            "6", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"f\" x1=\"0\" y1=\"1\" x2=\"1\" y2=\"0\"><stop offset=\"0%\" stop-color=\"#4c1d95\"/><stop offset=\"100%\" stop-color=\"#7c3aed\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#f)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".88\"/><ellipse cx=\"50\" cy=\"78\" rx=\"28\" ry=\"18\" fill=\"#fff\" opacity=\".8\"/></svg>",
            "7", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop offset=\"0%\" stop-color=\"#475569\"/><stop offset=\"100%\" stop-color=\"#1e293b\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#g)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".9\"/><ellipse cx=\"50\" cy=\"77\" rx=\"30\" ry=\"19\" fill=\"#fff\" opacity=\".84\"/></svg>",
            "8", "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><linearGradient id=\"h\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop offset=\"0%\" stop-color=\"#ec4899\"/><stop offset=\"100%\" stop-color=\"#a855f7\"/></linearGradient></defs><rect width=\"100\" height=\"100\" rx=\"22\" fill=\"url(#h)\"/><circle cx=\"50\" cy=\"38\" r=\"16\" fill=\"#fff\" opacity=\".92\"/><ellipse cx=\"50\" cy=\"78\" rx=\"28\" ry=\"18\" fill=\"#fff\" opacity=\".86\"/></svg>"
    );

    private final ProfilePictureStorage profilePictureStorage;

    public CVProfileImageSourceResolver(ProfilePictureStorage profilePictureStorage) {
        this.profilePictureStorage = profilePictureStorage;
    }

    public String resolvePdfSafeSource(String profilePicture) {
        if (profilePicture == null || profilePicture.isBlank()) {
            return null;
        }

        String trimmed = profilePicture.trim();
        if (trimmed.startsWith("data:image/")) {
            return trimmed;
        }

        String preset = resolvePresetSvg(trimmed);
        if (preset != null) {
            return svgDataUri(preset);
        }

        if (trimmed.startsWith("/api/files/profile-pictures/")) {
            String filename = trimmed.substring("/api/files/profile-pictures/".length());
            Path path = profilePictureStorage.resolveExistingFile(filename);
            return path == null ? null : fileDataUri(path);
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        return null;
    }

    private String resolvePresetSvg(String value) {
        Matcher matcher = PRESET_PATTERN.matcher(value);
        if (matcher.matches()) {
            return PRESET_SVGS.get(matcher.group(1));
        }

        matcher = LEGACY_PRESET_PATTERN.matcher(value);
        if (matcher.matches()) {
            return PRESET_SVGS.get(matcher.group(1));
        }
        return null;
    }

    private String svgDataUri(String svg) {
        return "data:image/svg+xml;charset=UTF-8," + java.net.URLEncoder.encode(svg, StandardCharsets.UTF_8);
    }

    private String fileDataUri(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            String mediaType = contentType == null || contentType.isBlank()
                    ? "application/octet-stream"
                    : contentType.toLowerCase(Locale.ROOT);
            return "data:" + mediaType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
