package com.education.platform.dto.certifications;

import com.education.platform.entities.certifications.Certification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CertificationDetailDTO(
        Long id,
        String title,
        String description,
        String category,
        Certification.Difficulty difficulty,
        Certification.Status status,
        BigDecimal price,
        LocalDate validFrom,
        LocalDate expiresAt,
        Integer durationMinutes,
        Double passingScore,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ExamResponseDTO> exams,
        String coverImageUrl
) {}
