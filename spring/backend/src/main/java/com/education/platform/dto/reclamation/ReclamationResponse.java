package com.education.platform.dto.reclamation;

import com.education.platform.entities.ReclamationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ReclamationResponse {

    private final Long id;
    /** Renseigné uniquement pour les listes administrateur. */
    private final String submitterUsername;
    private final String subject;
    private final String description;
    private final ReclamationStatus status;
    private final String adminResponse;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant reviewedAt;
}
