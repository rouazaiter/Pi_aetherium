package tn.esprit.backend.entities;

public enum ServiceRequestStatus {
    OPEN,      // Demande publiée, accepte des candidatures
    CLOSED,    // Demande terminée (après acceptation d'une candidature)
    EXPIRED    // Date d'expiration dépassée
}
