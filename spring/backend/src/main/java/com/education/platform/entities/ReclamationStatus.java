package com.education.platform.entities;

public enum ReclamationStatus {
    /** Soumise, en attente de l'équipe. */
    PENDING,
    /** Prise en charge par un administrateur. */
    IN_REVIEW,
    /** Réponse envoyée au client, dossier clos côté support. */
    RESOLVED
}
