package com.education.platform.services.implementations.portfolio;

import com.education.platform.common.ApiException;
import com.education.platform.dto.portfolio.CreateSkillRequest;
import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.repositories.portfolio.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillCatalogServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    private PortfolioMapper portfolioMapper;

    @InjectMocks
    private SkillCatalogServiceImpl skillCatalogService;

    @BeforeEach
    void setUp() {
        portfolioMapper = new PortfolioMapper();
        skillCatalogService = new SkillCatalogServiceImpl(skillRepository, portfolioMapper);
    }

    @Test
    void findOrCreateReturnsExistingSkillIgnoringCaseAndSpaces() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("  JAVA  ");
        request.setCategory(SkillCategory.BACKEND_DEVELOPMENT);

        Skill existing = Skill.builder()
                .id(7L)
                .name("Java")
                .normalizedName("java")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        when(skillRepository.findByNormalizedName("java")).thenReturn(Optional.of(existing));

        SkillSummaryDto result = skillCatalogService.findOrCreate(request);

        assertEquals(7L, result.getId());
        assertEquals("Java", result.getName());
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    void listCategoriesReturnsEnumValues() {
        List<SkillCategory> categories = skillCatalogService.listCategories();

        assertTrue(categories.contains(SkillCategory.FRONTEND_DEVELOPMENT));
        assertTrue(categories.contains(SkillCategory.OTHER));
        assertEquals(SkillCategory.values().length, categories.size());
    }

    @Test
    void listAllReturnsSkillsWithNullNormalizedNameAndMappedCategory() {
        Skill java = Skill.builder()
                .id(1L)
                .name("Java")
                .normalizedName(null)
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();
        Skill git = Skill.builder()
                .id(2L)
                .name("Git")
                .normalizedName(null)
                .category(SkillCategory.OTHER)
                .build();

        when(skillRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name")))
                .thenReturn(List.of(java, git));

        List<SkillSummaryDto> result = skillCatalogService.listAll();

        assertEquals(2, result.size());
        assertEquals("Java", result.get(0).getName());
        assertEquals(SkillCategory.BACKEND_DEVELOPMENT, result.get(0).getCategory());
        assertEquals(SkillCategory.OTHER, result.get(1).getCategory());
    }

    @Test
    void findOrCreateCreatesSkillWhenMissing() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("  GraphQL  ");
        request.setCategory(SkillCategory.BACKEND_DEVELOPMENT);
        request.setDescription(" API query language ");

        when(skillRepository.findByNormalizedName("graphql")).thenReturn(Optional.empty());
        when(skillRepository.findByNameIgnoreCase("GraphQL")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill skill = invocation.getArgument(0);
            skill.setId(11L);
            skill.syncNormalizedName();
            return skill;
        });

        SkillSummaryDto result = skillCatalogService.findOrCreate(request);

        ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class);
        verify(skillRepository).save(captor.capture());
        Skill saved = captor.getValue();

        assertEquals("GraphQL", saved.getName());
        assertEquals("graphql", saved.getNormalizedName());
        assertEquals(SkillCategory.BACKEND_DEVELOPMENT, saved.getCategory());
        assertEquals("API query language", saved.getDescription());
        assertEquals(11L, result.getId());
    }

    @Test
    void findOrCreateDefaultsCategoryToOther() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Figma");

        when(skillRepository.findByNormalizedName("figma")).thenReturn(Optional.empty());
        when(skillRepository.findByNameIgnoreCase("Figma")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill skill = invocation.getArgument(0);
            skill.setId(3L);
            skill.syncNormalizedName();
            return skill;
        });

        SkillSummaryDto result = skillCatalogService.findOrCreate(request);

        assertNotNull(result);
        ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class);
        verify(skillRepository).save(captor.capture());
        assertEquals(SkillCategory.OTHER, captor.getValue().getCategory());
    }

    @Test
    void requireSkillsByIdsRejectsDuplicates() {
        ApiException ex = assertThrows(ApiException.class,
                () -> skillCatalogService.requireSkillsByIds(List.of(1L, 1L)));

        assertEquals("La liste des competences contient des doublons ou des valeurs invalides", ex.getMessage());
    }
}
