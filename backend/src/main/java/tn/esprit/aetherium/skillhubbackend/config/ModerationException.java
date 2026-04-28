package tn.esprit.aetherium.skillhubbackend.config;

import java.util.List;

public class ModerationException extends RuntimeException {
    private final List<String> detectedWords;

    public ModerationException(List<String> detectedWords) {
        super("Content contains inappropriate language");
        this.detectedWords = detectedWords;
    }

    public List<String> getDetectedWords() { return detectedWords; }
}
