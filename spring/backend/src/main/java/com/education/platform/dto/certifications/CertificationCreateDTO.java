package com.education.platform.dto.certifications;

import com.education.platform.entities.certifications.Certification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CertificationCreateDTO(
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
        List<ExamCreateDTO> exams,
        String coverImageUrl
) {}
