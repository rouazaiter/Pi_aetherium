package tn.esprit.aetherium.skillhubbackend.services.implementations;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UrgencyDetectionServiceImpl {

    public enum UrgencyLevel { NONE, LOW, HIGH, CRITICAL }

    public record UrgencyResult(UrgencyLevel level, List<String> matchedKeywords) {}

    // CRITICAL — production/system down, data loss, security breach
    private static final List<String> CRITICAL_KEYWORDS = List.of(
        "production down", "prod down", "system down", "server down", "database down",
        "data loss", "data breach", "security breach", "hacked", "compromised",
        "critical error", "fatal error", "application crashed", "app crashed",
        "500 error", "error 500", "http 500", "internal server error",
        "out of memory", "oom", "memory leak", "deadlock",
        "urgent", "emergency", "critical", "immediately", "asap",
        "panne", "urgence", "critique", "serveur tombé", "base de données down"
    );

    // HIGH — significant issues affecting functionality
    private static final List<String> HIGH_KEYWORDS = List.of(
        "not working", "broken", "bug", "crash", "exception", "error",
        "failed", "failure", "cannot connect", "connection refused", "timeout",
        "403", "404", "401", "null pointer", "nullpointerexception",
        "stack overflow", "infinite loop", "performance issue", "slow",
        "help needed", "stuck", "blocked", "cannot deploy", "deployment failed",
        "ne fonctionne pas", "erreur", "problème", "bloqué", "aide"
    );

    public UrgencyResult detect(String title, String content) {
        if (title == null && content == null) return new UrgencyResult(UrgencyLevel.NONE, List.of());

        String combined = ((title == null ? "" : title) + " " + (content == null ? "" : content)).toLowerCase();
        java.util.List<String> matched = new java.util.ArrayList<>();

        for (String kw : CRITICAL_KEYWORDS) {
            if (combined.contains(kw.toLowerCase())) matched.add(kw);
        }
        if (!matched.isEmpty()) return new UrgencyResult(UrgencyLevel.CRITICAL, matched);

        for (String kw : HIGH_KEYWORDS) {
            if (combined.contains(kw.toLowerCase())) matched.add(kw);
        }
        if (!matched.isEmpty()) return new UrgencyResult(UrgencyLevel.HIGH, matched);

        return new UrgencyResult(UrgencyLevel.NONE, List.of());
    }
}
