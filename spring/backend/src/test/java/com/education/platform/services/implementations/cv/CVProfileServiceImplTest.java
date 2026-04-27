package com.education.platform.services.implementations.cv;

import com.education.platform.dto.cv.CVProfileResponse;
import com.education.platform.dto.cv.CVEducationDto;
import com.education.platform.dto.cv.CVExperienceDto;
import com.education.platform.dto.cv.CVLanguageDto;
import com.education.platform.dto.cv.UpdateCVProfileRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVProfile;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.cv.CVProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CVProfileServiceImplTest {

    @Mock
    private CVProfileRepository cvProfileRepository;

    @InjectMocks
    private CVProfileServiceImpl cvProfileService;

    @Test
    void updateForUserCreatesProfileWithDefaultsAndNormalizedTemplate() {
        User user = User.builder()
                .id(1L)
                .build();

        UpdateCVProfileRequest request = new UpdateCVProfileRequest();
        request.setHeadline("Backend Engineer");
        request.setPreferredTemplate("unsupported-template");
        request.setPhone("+111");
        request.setLocation("Tunis");
        request.setProfessionalSummary("Professional summary");
        request.setSelectedProjectIds(List.of(10L, 11L, 10L));
        request.setEducation(List.of(CVEducationDto.builder().school("University A").degree("BSc").build()));
        request.setExperience(List.of(CVExperienceDto.builder().company("Acme").role("Engineer").summary("Built APIs").build()));
        request.setLanguages(List.of(CVLanguageDto.builder().name("English").proficiency("Fluent").build()));

        when(cvProfileRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(cvProfileRepository.save(org.mockito.ArgumentMatchers.any(CVProfile.class)))
                .thenAnswer(invocation -> {
                    CVProfile saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        CVProfileResponse response = cvProfileService.updateForUser(user, request);

        ArgumentCaptor<CVProfile> captor = ArgumentCaptor.forClass(CVProfile.class);
        verify(cvProfileRepository).save(captor.capture());
        CVProfile saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals("Backend Engineer", saved.getHeadline());
        assertEquals("Professional summary", saved.getSummary());
        assertEquals("+111", saved.getPhone());
        assertEquals("Tunis", saved.getLocation());
        assertEquals("developer-minimal", saved.getPreferredTemplate());
        assertEquals("en", saved.getLanguage());
        assertEquals(Visibility.PRIVATE, saved.getVisibility());
        assertEquals(List.of(10L, 11L), saved.getSelectedProjectIds());
        assertEquals(1, saved.getEducation().size());
        assertEquals(1, saved.getExperience().size());
        assertEquals(1, saved.getLanguages().size());

        assertEquals(10L, response.getId());
        assertEquals("developer-minimal", response.getPreferredTemplate());
        assertEquals("en", response.getLanguage());
        assertEquals(Visibility.PRIVATE, response.getVisibility());
        assertEquals("Professional summary", response.getProfessionalSummary());
    }

    @Test
    void updateForUserUpdatesExistingProfileFields() {
        User user = User.builder()
                .id(2L)
                .build();

        CVProfile existing = CVProfile.builder()
                .id(20L)
                .user(user)
                .headline("Old headline")
                .summary("Old summary")
                .preferredTemplate("modern")
                .language("fr")
                .visibility(Visibility.PUBLIC)
                .build();

        UpdateCVProfileRequest request = new UpdateCVProfileRequest();
        request.setSummary("New summary");
        request.setLanguage("en");
        request.setVisibility(Visibility.FRIENDS_ONLY);

        when(cvProfileRepository.findByUser_Id(2L)).thenReturn(Optional.of(existing));
        when(cvProfileRepository.save(existing)).thenReturn(existing);

        CVProfileResponse response = cvProfileService.updateForUser(user, request);

        assertEquals("Old headline", existing.getHeadline());
        assertEquals("New summary", existing.getSummary());
        assertEquals("modern", existing.getPreferredTemplate());
        assertEquals("en", existing.getLanguage());
        assertEquals(Visibility.FRIENDS_ONLY, existing.getVisibility());

        assertEquals("Old headline", response.getHeadline());
        assertEquals("New summary", response.getSummary());
        assertEquals("New summary", response.getProfessionalSummary());
        assertEquals("modern", response.getPreferredTemplate());
        assertEquals("en", response.getLanguage());
        assertEquals(Visibility.FRIENDS_ONLY, response.getVisibility());
    }

    @Test
    void getForUserReturnsDefaultsWhenProfileIsMissing() {
        User user = User.builder()
                .id(3L)
                .build();

        when(cvProfileRepository.findByUser_Id(3L)).thenReturn(Optional.empty());

        CVProfileResponse response = cvProfileService.getForUser(user);

        assertNull(response.getId());
        assertNull(response.getHeadline());
        assertEquals("developer-minimal", response.getPreferredTemplate());
        assertEquals("en", response.getLanguage());
        assertEquals(Visibility.PRIVATE, response.getVisibility());
    }
}
