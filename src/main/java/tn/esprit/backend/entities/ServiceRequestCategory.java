package tn.esprit.backend.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceRequestCategory {
    DEVELOPPEMENT_LOGICIEL("Software Development"),
    RESEAUX_ET_SYSTEMES("Networks and Systems"),
    CYBERSECURITE("Cybersecurity"),
    DATA_INTELLIGENCE_ARTIFICIELLE("Data / Artificial Intelligence"),
    CLOUD_COMPUTING("Cloud Computing");

    private final String label;

    ServiceRequestCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ServiceRequestCategory fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String normalized = rawValue.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        for (ServiceRequestCategory category : values()) {
            if (category.label.equalsIgnoreCase(normalized)
                    || category.name().equalsIgnoreCase(normalized)
                    || category.name().replace('_', ' ').equalsIgnoreCase(normalized)
                    || matchesLegacyFrenchLabel(category, normalized)) {
                return category;
            }
        }

        throw new IllegalArgumentException("Unsupported category: " + rawValue);
    }

    private static boolean matchesLegacyFrenchLabel(ServiceRequestCategory category, String rawValue) {
        return switch (category) {
            case DEVELOPPEMENT_LOGICIEL -> "Développement logiciel".equalsIgnoreCase(rawValue)
                    || "Developpement logiciel".equalsIgnoreCase(rawValue);
            case RESEAUX_ET_SYSTEMES -> "Réseaux et systèmes".equalsIgnoreCase(rawValue)
                    || "Reseaux et systemes".equalsIgnoreCase(rawValue);
            case CYBERSECURITE -> "Cybersécurité".equalsIgnoreCase(rawValue)
                    || "Cybersecurite".equalsIgnoreCase(rawValue);
            case DATA_INTELLIGENCE_ARTIFICIELLE -> "Data / Intelligence Artificielle".equalsIgnoreCase(rawValue);
            case CLOUD_COMPUTING -> false;
        };
    }
}