package com.education.platform.services.implementations.cv;

import com.education.platform.dto.cv.CVDraftResponse;
import com.education.platform.dto.cv.CVPreviewMetaDto;
import com.education.platform.dto.cv.CVPreviewProfileDto;
import com.education.platform.dto.cv.CVPreviewProjectDto;
import com.education.platform.dto.cv.CVPreviewResponse;
import com.education.platform.dto.cv.CVPreviewSkillDto;
import com.education.platform.dto.cv.CVPreviewSkillGroupDto;
import com.education.platform.dto.cv.UpdateCVDraftRequest;
import com.education.platform.dto.cv.UpdateCVSectionRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVSection;
import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.cv.CVBuilderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CVDraftServiceImplTest {

    @Mock
    private CVDraftRepository cvDraftRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private CVBuilderService cvBuilderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void generateForUserBuildsDraftFromPreview() {
        CVDraftServiceImpl cvDraftService = new CVDraftServiceImpl(
                cvDraftRepository,
                portfolioRepository,
                cvBuilderService,
                objectMapper
        );

        User user = User.builder().id(7L).build();
        Portfolio portfolio = Portfolio.builder().id(11L).build();
        CVPreviewResponse preview = CVPreviewResponse.builder()
                .profile(CVPreviewProfileDto.builder().fullName("Jane Doe").preferredTemplate("modern").build())
                .skillsByCategory(List.of(CVPreviewSkillGroupDto.builder()
                        .category("BACKEND_DEVELOPMENT")
                        .skills(List.of(CVPreviewSkillDto.builder().id(1L).name("Java").build()))
                        .build()))
                .projects(List.of(CVPreviewProjectDto.builder()
                        .id(101L)
                        .title("API Platform")
                        .imageUrl("https://cdn.example.com/project.png")
                        .build()))
                .meta(CVPreviewMetaDto.builder().estimatedPages(2).exceedsTwoPages(false).build())
                .build();

        when(cvBuilderService.buildForUser(user)).thenReturn(preview);
        when(portfolioRepository.findByUser_Id(7L)).thenReturn(Optional.of(portfolio));
        when(cvDraftRepository.save(any(CVDraft.class))).thenAnswer(invocation -> {
            CVDraft saved = invocation.getArgument(0);
            saved.setId(20L);
            saved.setCreatedAt(LocalDateTime.of(2026, 4, 25, 10, 0));
            saved.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 10, 0));
            long sectionId = 1L;
            for (CVSection section : saved.getSections()) {
                section.setId(sectionId++);
            }
            return saved;
        });

        CVDraftResponse response = cvDraftService.generateForUser(user);

        assertEquals(20L, response.getId());
        assertEquals("ATS_MINIMAL", response.getTheme());
        assertEquals(11L, response.getPortfolioId());
        assertNotNull(response.getSettings());
        assertEquals(false, response.getSettings().get("showProjectImages").asBoolean());
        assertEquals(3, response.getSections().size());
        assertEquals(CVSectionType.PROFILE, response.getSections().get(0).getType());
        assertEquals(CVSectionType.SKILLS, response.getSections().get(1).getType());
        assertEquals(CVSectionType.PROJECTS, response.getSections().get(2).getType());
        assertEquals(
                "https://cdn.example.com/project.png",
                response.getSections().get(2).getContent().get(0).get("imageUrl").asText()
        );
    }

    @Test
    void getLatestForUserReturnsMostRecentDraft() {
        CVDraftServiceImpl cvDraftService = new CVDraftServiceImpl(
                cvDraftRepository,
                portfolioRepository,
                cvBuilderService,
                objectMapper
        );

        User user = User.builder().id(9L).build();
        CVDraft draft = CVDraft.builder()
                .id(30L)
                .user(user)
                .theme("ATS_MINIMAL")
                .settingsJson("{\"theme\":\"ATS_MINIMAL\"}")
                .createdAt(LocalDateTime.of(2026, 4, 25, 11, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 25, 11, 30))
                .build();
        draft.addSection(CVSection.builder()
                .id(1L)
                .type(CVSectionType.PROFILE)
                .title("Profile")
                .orderIndex(0)
                .visible(true)
                .contentJson("{\"fullName\":\"Jane Doe\"}")
                .build());

        when(cvDraftRepository.findTopByUser_IdOrderByUpdatedAtDescIdDesc(9L)).thenReturn(Optional.of(draft));

        CVDraftResponse response = cvDraftService.getLatestForUser(user);

        assertEquals(30L, response.getId());
        assertEquals("ATS_MINIMAL", response.getTheme());
        assertEquals(1, response.getSections().size());
        assertEquals("Jane Doe", response.getSections().get(0).getContent().get("fullName").asText());
    }

    @Test
    void updateForUserReplacesMutableDraftFieldsAndSections() throws Exception {
        CVDraftServiceImpl cvDraftService = new CVDraftServiceImpl(
                cvDraftRepository,
                portfolioRepository,
                cvBuilderService,
                objectMapper
        );

        User user = User.builder().id(10L).build();
        Portfolio portfolio = Portfolio.builder().id(12L).build();
        CVDraft draft = CVDraft.builder()
                .id(40L)
                .user(user)
                .portfolio(portfolio)
                .theme("ATS_MINIMAL")
                .settingsJson("{\"theme\":\"ATS_MINIMAL\",\"showProjectImages\":false,\"estimatedPages\":2}")
                .createdAt(LocalDateTime.of(2026, 4, 25, 9, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 25, 9, 30))
                .build();
        draft.addSection(CVSection.builder()
                .id(1L)
                .type(CVSectionType.PROFILE)
                .title("Profile")
                .orderIndex(0)
                .visible(true)
                .contentJson("{\"fullName\":\"Jane Doe\"}")
                .build());

        UpdateCVSectionRequest sectionRequest = new UpdateCVSectionRequest();
        sectionRequest.setType(CVSectionType.PROJECTS);
        sectionRequest.setTitle("Selected Projects");
        sectionRequest.setOrderIndex(3);
        sectionRequest.setVisible(true);
        JsonNode sectionContent = objectMapper.readTree("[{\"id\":101,\"title\":\"API Platform\"}]");
        sectionRequest.setContent(sectionContent);

        UpdateCVDraftRequest request = new UpdateCVDraftRequest();
        request.setTheme("modern");
        request.setSettings(objectMapper.readTree("{\"estimatedPages\":1}"));
        request.setSections(List.of(sectionRequest));

        when(cvDraftRepository.findByIdAndUser_Id(40L, 10L)).thenReturn(Optional.of(draft));
        when(cvDraftRepository.save(any(CVDraft.class))).thenAnswer(invocation -> {
            CVDraft saved = invocation.getArgument(0);
            saved.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 10, 0));
            long sectionId = 100L;
            for (CVSection section : saved.getSections()) {
                if (section.getId() == null) {
                    section.setId(sectionId++);
                }
            }
            return saved;
        });

        CVDraftResponse response = cvDraftService.updateForUser(user, 40L, request);

        assertEquals(40L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals(12L, response.getPortfolioId());
        assertEquals("ATS_MINIMAL", response.getTheme());
        assertEquals("ATS_MINIMAL", response.getSettings().get("theme").asText());
        assertEquals(false, response.getSettings().get("showProjectImages").asBoolean());
        assertEquals(1, response.getSettings().get("estimatedPages").asInt());
        assertEquals(1, response.getSections().size());
        assertEquals(CVSectionType.PROJECTS, response.getSections().get(0).getType());
        assertEquals("Selected Projects", response.getSections().get(0).getTitle());
        assertEquals("API Platform", response.getSections().get(0).getContent().get(0).get("title").asText());
    }

    @Test
    void updateForUserThrowsWhenDraftDoesNotBelongToCurrentUser() {
        CVDraftServiceImpl cvDraftService = new CVDraftServiceImpl(
                cvDraftRepository,
                portfolioRepository,
                cvBuilderService,
                objectMapper
        );

        User user = User.builder().id(11L).build();
        UpdateCVDraftRequest request = new UpdateCVDraftRequest();
        when(cvDraftRepository.findByIdAndUser_Id(41L, 11L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> cvDraftService.updateForUser(user, 41L, request));

        assertEquals("CV draft not found", exception.getMessage());
    }
}
