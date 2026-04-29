package com.education.platform.dto.reclamation;

import com.education.platform.entities.ReclamationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReclamationUpdateRequest {

    @NotNull(message = "Statut requis")
    private ReclamationStatus status;

    /** Réponse visible par le client (recommandée pour RESOLVED). */
    private String adminResponse;
}
