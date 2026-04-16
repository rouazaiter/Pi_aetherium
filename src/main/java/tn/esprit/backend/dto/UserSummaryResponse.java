package tn.esprit.backend.dto;

public record UserSummaryResponse(
        Long id,
        String username,
        String email
) {
}