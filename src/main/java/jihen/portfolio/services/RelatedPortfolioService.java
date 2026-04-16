package jihen.portfolio.services;


import jihen.portfolio.dtos.RelatedPortfolioDto;
import jihen.portfolio.dtos.RelatedPortfoliosResponseDto;
import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.Skill;
import jihen.portfolio.enums.PortfolioVisibility;
import jihen.portfolio.repositories.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatedPortfolioService {

    private final PortfolioRepository portfolioRepository;

    public RelatedPortfoliosResponseDto getRelatedPortfolios(Long portfolioId, Integer limit, String excludeIds) {
        Portfolio sourcePortfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));

        Set<Long> excludedIds = parseExcludedIds(excludeIds);
        excludedIds.add(portfolioId);

        Set<String> sourceSkills = extractSkillNames(sourcePortfolio.getSkills());

        List<Portfolio> publicPortfolios = portfolioRepository.findByVisibility(PortfolioVisibility.PUBLIC);

        Set<RelatedPortfolioDto> relatedPortfolios = publicPortfolios.stream()
                .filter(portfolio -> portfolio.getId() != null)
                .filter(portfolio -> !excludedIds.contains(portfolio.getId()))
                .map(portfolio -> buildRelatedPortfolioDto(portfolio, sourceSkills))
                .filter(dto -> dto.getSimilarityScore() > 0)
                .sorted(Comparator.comparing(RelatedPortfolioDto::getSimilarityScore).reversed())
                .limit(limit != null && limit > 0 ? limit : 4)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return RelatedPortfoliosResponseDto.builder()
                .sourcePortfolioId(portfolioId)
                .count(relatedPortfolios.size())
                .portfolios(relatedPortfolios)
                .build();
    }

    private RelatedPortfolioDto buildRelatedPortfolioDto(Portfolio candidate, Set<String> sourceSkills) {
        Set<String> candidateSkills = extractSkillNames(candidate.getSkills());

        Set<String> matchedSkills = candidateSkills.stream()
                .filter(sourceSkills::contains)
                .limit(3)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int similarityScore = calculateSimilarityScore(sourceSkills, candidateSkills);

        return RelatedPortfolioDto.builder()
                .id(candidate.getId())
                .title(candidate.getTitle())
                .bio(candidate.getBio())
                .profilePicture(candidate.getProfilePicture())
                .location(candidate.getLocation())
                .similarityScore(similarityScore)
                .matchedSkills(matchedSkills)
                .build();
    }

    private int calculateSimilarityScore(Set<String> sourceSkills, Set<String> candidateSkills) {
        if (sourceSkills == null || sourceSkills.isEmpty() || candidateSkills == null || candidateSkills.isEmpty()) {
            return 0;
        }

        long commonSkillsCount = candidateSkills.stream()
                .filter(sourceSkills::contains)
                .count();

        if (commonSkillsCount == 0) {
            return 0;
        }

        Set<String> union = new HashSet<>(sourceSkills);
        union.addAll(candidateSkills);

        double score = ((double) commonSkillsCount / union.size()) * 100;

        return (int) Math.round(score);
    }

    private Set<String> extractSkillNames(Set<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return new HashSet<>();
        }

        return skills.stream()
                .map(Skill::getName)
                .filter(Objects::nonNull)
                .map(name -> name.trim().toLowerCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> parseExcludedIds(String excludeIds) {
        if (excludeIds == null || excludeIds.isBlank()) {
            return new HashSet<>();
        }

        return Arrays.stream(excludeIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toCollection(HashSet::new));
    }
}

