package com.education.platform.services.implementations.portfolio;

import com.education.platform.common.ApiException;
import com.education.platform.dto.portfolio.CreateSkillRequest;
import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.repositories.portfolio.SkillRepository;
import com.education.platform.services.interfaces.portfolio.SkillCatalogService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class SkillCatalogServiceImpl implements SkillCatalogService {

    private final SkillRepository skillRepository;
    private final PortfolioMapper portfolioMapper;

    public SkillCatalogServiceImpl(SkillRepository skillRepository, PortfolioMapper portfolioMapper) {
        this.skillRepository = skillRepository;
        this.portfolioMapper = portfolioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillSummaryDto> listAll() {
        return skillRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(portfolioMapper::toSkillSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillSummaryDto> search(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return listAll();
        }
        return skillRepository.findByNameContainingIgnoreCaseOrderByNameAsc(normalizedQuery).stream()
                .map(portfolioMapper::toSkillSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillCategory> listCategories() {
        return Arrays.asList(SkillCategory.values());
    }

    @Override
    @Transactional
    public SkillSummaryDto findOrCreate(CreateSkillRequest request) {
        String rawName = request == null ? null : request.getName();
        String normalizedName = Skill.normalizeName(rawName);
        if (normalizedName == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le nom de la competence est obligatoire");
        }

        Skill existing = skillRepository.findByNormalizedName(normalizedName)
                .or(() -> skillRepository.findByNameIgnoreCase(rawName == null ? null : rawName.trim()))
                .orElse(null);
        if (existing != null) {
            if (existing.getNormalizedName() == null) {
                existing.setNormalizedName(normalizedName);
            }
            return portfolioMapper.toSkillSummary(existing);
        }

        Skill skill = Skill.builder()
                .name(rawName.trim())
                .category(request.getCategory() == null ? SkillCategory.OTHER : request.getCategory())
                .description(request.getDescription() == null ? null : request.getDescription().trim())
                .build();

        Skill saved = skillRepository.save(skill);
        return portfolioMapper.toSkillSummary(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Skill> requireSkillsByIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        LinkedHashSet<Long> uniqueIds = skillIds.stream()
                .filter(id -> id != null)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        if (uniqueIds.size() != skillIds.stream().filter(id -> id != null).count()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La liste des competences contient des doublons ou des valeurs invalides");
        }

        List<Skill> skills = skillRepository.findByIdIn(uniqueIds);
        if (skills.size() != uniqueIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Une ou plusieurs competences sont introuvables");
        }

        return new LinkedHashSet<>(skills);
    }
}
