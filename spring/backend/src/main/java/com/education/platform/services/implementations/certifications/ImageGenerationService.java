package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Generates a cover image URL for a certification.
 *
 * Strategy: Unsplash Source API — free, no API key, instant, high-quality photos.
 * URL format: https://source.unsplash.com/800x450/?{keywords}
 *
 * The seed is derived from the title so the same cert always gets the same image.
 * We append a unique sig so Unsplash doesn't cache-bust on every request.
 */
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    public String generateCoverImageUrl(String title, String category, String difficulty) {
        String keywords = buildKeywords(title, category, difficulty);
        return buildUnsplashUrl(keywords, title);
    }

    // ── Keyword mapping per category ──────────────────────────────────────────

    private String buildKeywords(String title, String category, String difficulty) {
        String cat = category != null ? category.toLowerCase() : "";

        // Category-specific keywords that produce great Unsplash results
        if (cat.contains("java") || cat.contains("spring"))
            return "programming,code,developer,technology,computer";
        if (cat.contains("python"))
            return "python,programming,data,technology,code";
        if (cat.contains("javascript") || cat.contains("js") || cat.contains("react") || cat.contains("angular") || cat.contains("vue"))
            return "web,development,code,technology,screen";
        if (cat.contains("web") || cat.contains("html") || cat.contains("css") || cat.contains("frontend"))
            return "web,design,technology,computer,screen";
        if (cat.contains("docker") || cat.contains("devops") || cat.contains("kubernetes") || cat.contains("ci"))
            return "server,infrastructure,technology,cloud,network";
        if (cat.contains("sql") || cat.contains("database") || cat.contains("db") || cat.contains("mongo"))
            return "database,data,server,technology,storage";
        if (cat.contains("cloud") || cat.contains("aws") || cat.contains("azure") || cat.contains("gcp"))
            return "cloud,technology,server,network,data";
        if (cat.contains("security") || cat.contains("cyber") || cat.contains("hack"))
            return "security,cyber,technology,lock,protection";
        if (cat.contains("machine learning") || cat.contains("ml") || cat.contains("deep learning"))
            return "artificial,intelligence,technology,neural,brain";
        if (cat.contains("ai") || cat.contains("artificial"))
            return "artificial,intelligence,robot,technology,future";
        if (cat.contains("data") || cat.contains("analytics") || cat.contains("bi"))
            return "data,analytics,chart,technology,business";
        if (cat.contains("mobile") || cat.contains("android") || cat.contains("ios") || cat.contains("flutter"))
            return "mobile,smartphone,app,technology,phone";
        if (cat.contains("network") || cat.contains("cisco") || cat.contains("ccna"))
            return "network,server,technology,infrastructure,cable";
        if (cat.contains("php") || cat.contains("laravel") || cat.contains("symfony"))
            return "programming,code,web,developer,technology";
        if (cat.contains("backend") || cat.contains("api") || cat.contains("microservice"))
            return "server,backend,technology,code,developer";
        if (cat.contains("design") || cat.contains("ui") || cat.contains("ux"))
            return "design,creative,interface,technology,art";
        if (cat.contains("agile") || cat.contains("scrum") || cat.contains("project"))
            return "teamwork,business,meeting,office,collaboration";
        if (cat.contains("math") || cat.contains("algorithm") || cat.contains("computer science"))
            return "mathematics,science,technology,abstract,code";
        if (cat.contains("programming") || cat.contains("software") || cat.contains("code"))
            return "programming,code,developer,technology,computer";

        // Title-based fallback keywords
        String titleLower = title != null ? title.toLowerCase() : "";
        if (titleLower.contains("certif") || titleLower.contains("exam"))
            return "education,certificate,achievement,success,learning";

        // Generic tech
        return "technology,computer,code,digital,innovation";
    }

    // ── Unsplash Source URL ───────────────────────────────────────────────────

    private String buildUnsplashUrl(String keywords, String title) {
        try {
            // Use a deterministic sig so the same cert always gets the same image
            // but different certs get different images
            int sig = Math.abs((title != null ? title : "cert").hashCode() % 1000);

            String encoded = URLEncoder.encode(keywords, StandardCharsets.UTF_8);

            // Unsplash Source: free, no auth, instant redirect to a real photo
            // Adding &sig= makes it deterministic per certification
            return "https://source.unsplash.com/800x450/?" + encoded + "&sig=" + sig;

        } catch (Exception e) {
            // Absolute fallback — a generic tech photo
            return "https://source.unsplash.com/800x450/?technology,computer";
        }
    }
}
