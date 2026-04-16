package jihen.portfolio.services;
import jihen.portfolio.dtos.VisitorProfile;
import org.springframework.stereotype.Service;

@Service
public class VisitorAnalyzerService {

    public VisitorProfile analyzeVisitor(String userAgent, String referer, String visitorParam) {
        VisitorProfile profile = new VisitorProfile();

        // 1. Priorité au paramètre URL (le plus fiable)
        if (visitorParam != null) {
            switch (visitorParam.toLowerCase()) {
                case "recruiter":
                    profile.setType("RECRUITER");
                    profile.setPriority("EXPERIENCE");
                    break;
                case "client":
                    profile.setType("CLIENT");
                    profile.setPriority("AVAILABILITY");
                    break;
                case "freelance":
                    profile.setType("FREELANCE");
                    profile.setPriority("SKILLS");
                    break;
                default:
                    profile.setType("DEFAULT");
                    profile.setPriority("PROJECTS");
            }
            return profile;
        }

        // 2. Détection par Referer
        if (referer != null) {
            if (referer.contains("linkedin")) {
                profile.setType("RECRUITER");
                profile.setSource("LINKEDIN");
                profile.setPriority("EXPERIENCE");
            } else if (referer.contains("github")) {
                profile.setType("FREELANCE");
                profile.setSource("GITHUB");
                profile.setPriority("SKILLS");
            } else if (referer.contains("google")) {
                profile.setType("CLIENT");
                profile.setSource("GOOGLE");
                profile.setPriority("AVAILABILITY");
            }
        }

        return profile;
    }
}