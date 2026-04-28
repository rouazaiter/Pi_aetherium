package com.education.plateform.dto;

public record UserSummaryResponse(
        Long id,
        String username,
        String email
) {
}