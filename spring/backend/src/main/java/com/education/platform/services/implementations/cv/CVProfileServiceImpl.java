package com.education.platform.services.implementations.cv;

import com.education.platform.dto.cv.CVProfileResponse;
import com.education.platform.dto.cv.CVEducationDto;
import com.education.platform.dto.cv.CVExperienceDto;
import com.education.platform.dto.cv.CVLanguageDto;
import com.education.platform.dto.cv.UpdateCVProfileRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVEducationEntry;
import com.education.platform.entities.cv.CVExperienceEntry;
import com.education.platform.entities.cv.CVLanguageEntry;
import com.education.platform.entities.cv.CVProfile;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.cv.CVProfileRepository;
import com.education.platform.services.interfaces.cv.CVProfileService;
import com.education.platform.services.interfaces.cv.CVTemplateConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CVProfileServiceImpl implements CVProfileService {

    private static final String DEFAULT_LANGUAGE = "en";

    private final CVProfileRepository cvProfileRepository;

    public CVProfileServiceImpl(CVProfileRepository cvProfileRepository) {
        this.cvProfileRepository = cvProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CVProfileResponse getForUser(User user) {
        return toResponse(cvProfileRepository.findByUser_Id(user.getId()).orElse(null));
    }

    @Override
    @Transactional
    public CVProfileResponse updateForUser(User user, UpdateCVProfileRequest request) {
        CVProfile cvProfile = cvProfileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> CVProfile.builder()
                        .user(user)
                        .preferredTemplate(CVTemplateConstants.DEFAULT_TEMPLATE)
                        .language(DEFAULT_LANGUAGE)
                        .visibility(Visibility.PRIVATE)
                        .build());

        if (request.getHeadline() != null) {
            cvProfile.setHeadline(request.getHeadline());
        }
        if (request.getSummary() != null) {
            cvProfile.setSummary(request.getSummary());
        }
        if (request.getProfessionalSummary() != null) {
            cvProfile.setSummary(request.getProfessionalSummary());
        }
        if (request.getPhone() != null) {
            cvProfile.setPhone(request.getPhone());
        }
        if (request.getLocation() != null) {
            cvProfile.setLocation(request.getLocation());
        }
        if (request.getPreferredTemplate() != null) {
            cvProfile.setPreferredTemplate(CVTemplateConstants.normalizeTemplate(request.getPreferredTemplate()));
        }
        if (request.getLanguage() != null) {
            cvProfile.setLanguage(request.getLanguage());
        }
        if (request.getVisibility() != null) {
            cvProfile.setVisibility(request.getVisibility());
        }
        if (request.getSelectedProjectIds() != null) {
            cvProfile.setSelectedProjectIds(request.getSelectedProjectIds().stream()
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        if (request.getEducation() != null) {
            cvProfile.setEducation(request.getEducation().stream()
                    .map(this::toEducationEntry)
                    .filter(this::hasEducationContent)
                    .collect(Collectors.toList()));
        }
        if (request.getExperience() != null) {
            cvProfile.setExperience(request.getExperience().stream()
                    .map(this::toExperienceEntry)
                    .filter(this::hasExperienceContent)
                    .collect(Collectors.toList()));
        }
        if (request.getLanguages() != null) {
            cvProfile.setLanguages(request.getLanguages().stream()
                    .map(this::toLanguageEntry)
                    .filter(this::hasLanguageContent)
                    .collect(Collectors.toList()));
        }

        return toResponse(cvProfileRepository.save(cvProfile));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CVProfile> findEntityForUser(User user) {
        return cvProfileRepository.findByUser_Id(user.getId());
    }

    private CVProfileResponse toResponse(CVProfile cvProfile) {
        if (cvProfile == null) {
            return CVProfileResponse.builder()
                    .preferredTemplate(CVTemplateConstants.DEFAULT_TEMPLATE)
                    .language(DEFAULT_LANGUAGE)
                    .visibility(Visibility.PRIVATE)
                    .build();
        }

        return CVProfileResponse.builder()
                .id(cvProfile.getId())
                .headline(cvProfile.getHeadline())
                .summary(cvProfile.getSummary())
                .professionalSummary(cvProfile.getSummary())
                .phone(cvProfile.getPhone())
                .location(cvProfile.getLocation())
                .preferredTemplate(CVTemplateConstants.normalizeTemplate(cvProfile.getPreferredTemplate()))
                .language(defaultIfBlank(cvProfile.getLanguage(), DEFAULT_LANGUAGE))
                .visibility(cvProfile.getVisibility() == null ? Visibility.PRIVATE : cvProfile.getVisibility())
                .selectedProjectIds(cvProfile.getSelectedProjectIds())
                .education(cvProfile.getEducation() == null ? java.util.List.of() : cvProfile.getEducation().stream().map(this::toEducationDto).toList())
                .experience(cvProfile.getExperience() == null ? java.util.List.of() : cvProfile.getExperience().stream().map(this::toExperienceDto).toList())
                .languages(cvProfile.getLanguages() == null ? java.util.List.of() : cvProfile.getLanguages().stream().map(this::toLanguageDto).toList())
                .createdAt(cvProfile.getCreatedAt())
                .updatedAt(cvProfile.getUpdatedAt())
                .build();
    }

    private CVEducationEntry toEducationEntry(CVEducationDto dto) {
        return CVEducationEntry.builder()
                .school(dto.getSchool())
                .degree(dto.getDegree())
                .fieldOfStudy(dto.getFieldOfStudy())
                .location(dto.getLocation())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .current(dto.getCurrent())
                .description(dto.getDescription())
                .build();
    }

    private CVExperienceEntry toExperienceEntry(CVExperienceDto dto) {
        return CVExperienceEntry.builder()
                .company(dto.getCompany())
                .role(dto.getRole())
                .location(dto.getLocation())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .current(dto.getCurrent())
                .summary(dto.getSummary())
                .build();
    }

    private CVLanguageEntry toLanguageEntry(CVLanguageDto dto) {
        return CVLanguageEntry.builder()
                .name(dto.getName())
                .proficiency(dto.getProficiency())
                .build();
    }

    private CVEducationDto toEducationDto(CVEducationEntry entry) {
        return CVEducationDto.builder()
                .school(entry.getSchool())
                .degree(entry.getDegree())
                .fieldOfStudy(entry.getFieldOfStudy())
                .location(entry.getLocation())
                .startDate(entry.getStartDate())
                .endDate(entry.getEndDate())
                .current(entry.getCurrent())
                .description(entry.getDescription())
                .build();
    }

    private CVExperienceDto toExperienceDto(CVExperienceEntry entry) {
        return CVExperienceDto.builder()
                .company(entry.getCompany())
                .role(entry.getRole())
                .location(entry.getLocation())
                .startDate(entry.getStartDate())
                .endDate(entry.getEndDate())
                .current(entry.getCurrent())
                .summary(entry.getSummary())
                .build();
    }

    private CVLanguageDto toLanguageDto(CVLanguageEntry entry) {
        return CVLanguageDto.builder()
                .name(entry.getName())
                .proficiency(entry.getProficiency())
                .build();
    }

    private boolean hasEducationContent(CVEducationEntry entry) {
        return entry != null && (
                hasText(entry.getSchool())
                        || hasText(entry.getDegree())
                        || hasText(entry.getFieldOfStudy())
                        || hasText(entry.getDescription())
                        || entry.getStartDate() != null
                        || entry.getEndDate() != null
        );
    }

    private boolean hasExperienceContent(CVExperienceEntry entry) {
        return entry != null && (
                hasText(entry.getCompany())
                        || hasText(entry.getRole())
                        || hasText(entry.getSummary())
                        || entry.getStartDate() != null
                        || entry.getEndDate() != null
        );
    }

    private boolean hasLanguageContent(CVLanguageEntry entry) {
        return entry != null && (hasText(entry.getName()) || hasText(entry.getProficiency()));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
