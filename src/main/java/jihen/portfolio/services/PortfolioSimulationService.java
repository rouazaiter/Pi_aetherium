package jihen.portfolio.services;


import jihen.portfolio.dtos.PortfolioScenarioDto;
import jihen.portfolio.dtos.PortfolioSimulationResponseDto;
import jihen.portfolio.dtos.SimulatedProjectDto;
import jihen.portfolio.dtos.SimulationSkillDto;
import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.PortfolioProject;
import jihen.portfolio.entities.PortfolioScenario;
import jihen.portfolio.entities.ScenarioSkill;
import jihen.portfolio.entities.Skill;
import jihen.portfolio.repositories.PortfolioRepository;
import jihen.portfolio.repositories.PortfolioScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    public class PortfolioSimulationService {

        private final PortfolioRepository portfolioRepository;
        private final PortfolioScenarioRepository portfolioScenarioRepository;

        public Set<PortfolioScenarioDto> getActiveScenarios() {
            return portfolioScenarioRepository.findByActiveTrue()
                    .stream()
                    .map(this::mapScenarioToDto)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        public PortfolioSimulationResponseDto simulate(Long portfolioId, String scenarioKey) {
            Portfolio portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));

            PortfolioScenario scenario = portfolioScenarioRepository.findByScenarioKeyAndActiveTrue(scenarioKey)
                    .orElseThrow(() -> new RuntimeException("Scenario not found or inactive: " + scenarioKey));

            Map<String, Integer> scenarioSkillWeights = scenario.getScenarioSkills()
                    .stream()
                    .collect(Collectors.toMap(
                            scenarioSkill -> normalize(scenarioSkill.getSkillName()),
                            ScenarioSkill::getWeight,
                            Integer::max
                    ));

            Set<SimulationSkillDto> recommendedSkills = extractRelevantSkills(portfolio, scenarioSkillWeights);

            Set<SimulatedProjectDto> recommendedProjects = portfolio.getProjects()
                    .stream()
                    .map(project -> mapProjectToSimulation(project, scenarioSkillWeights))
                    .sorted(Comparator.comparing(SimulatedProjectDto::getScore).reversed())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Integer globalScore = calculateGlobalScore(recommendedProjects);

            String reason = buildReason(scenario, recommendedSkills, recommendedProjects);

            return PortfolioSimulationResponseDto.builder()
                    .scenarioKey(scenario.getScenarioKey())
                    .label(scenario.getLabel())
                    .description(scenario.getDescription())
                    .globalScore(globalScore)
                    .reason(reason)
                    .recommendedSkills(recommendedSkills)
                    .recommendedProjects(recommendedProjects)
                    .build();
        }

        private Set<SimulationSkillDto> extractRelevantSkills(Portfolio portfolio, Map<String, Integer> scenarioSkillWeights) {
            if (portfolio.getSkills() == null || portfolio.getSkills().isEmpty()) {
                return new LinkedHashSet<>();
            }

            return portfolio.getSkills()
                    .stream()
                    .filter(skill -> skill.getName() != null)
                    .filter(skill -> scenarioSkillWeights.containsKey(normalize(skill.getName())))
                    .sorted((skill1, skill2) -> Integer.compare(
                            scenarioSkillWeights.get(normalize(skill2.getName())),
                            scenarioSkillWeights.get(normalize(skill1.getName()))
                    ))
                    .map(skill -> SimulationSkillDto.builder()
                            .id(skill.getId())
                            .name(skill.getName())
                            .category(skill.getTypecategory() != null ? skill.getTypecategory().name() : null)
                            .weight(scenarioSkillWeights.get(normalize(skill.getName())))
                            .build())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        private SimulatedProjectDto mapProjectToSimulation(PortfolioProject project, Map<String, Integer> scenarioSkillWeights) {
            int score = 0;
            Set<String> matchedSkills = new LinkedHashSet<>();

            if (project.getSkills() != null && !project.getSkills().isEmpty()) {
                for (Skill skill : project.getSkills()) {
                    if (skill.getName() == null) {
                        continue;
                    }

                    String normalizedSkillName = normalize(skill.getName());

                    if (scenarioSkillWeights.containsKey(normalizedSkillName)) {
                        score += scenarioSkillWeights.get(normalizedSkillName);
                        matchedSkills.add(skill.getName());
                    }
                }
            }

            if (project.getProjectUrl() != null && !project.getProjectUrl().isBlank()) {
                score += 10;
            }

            if (Boolean.TRUE.equals(project.getPinned())) {
                score += 8;
            }

            if (project.getLikes() != null) {
                score += Math.min(project.getLikes(), 15);
            }

            if (project.getViews() != null) {
                score += Math.min(project.getViews() / 20, 15);
            }

            if (project.getCreatedAt() != null) {
                long days = ChronoUnit.DAYS.between(project.getCreatedAt().toLocalDate(), LocalDate.now());

                if (days <= 30) {
                    score += 10;
                } else if (days <= 90) {
                    score += 6;
                } else if (days <= 180) {
                    score += 3;
                }
            }

            if (score > 100) {
                score = 100;
            }

            return SimulatedProjectDto.builder()
                    .id(project.getId())
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .projectUrl(project.getProjectUrl())
                    .score(score)
                    .matchedSkills(matchedSkills)
                    .build();
        }

        private Integer calculateGlobalScore(Set<SimulatedProjectDto> projects) {
            if (projects == null || projects.isEmpty()) {
                return 0;
            }

            return (int) Math.round(
                    projects.stream()
                            .limit(3)
                            .mapToInt(SimulatedProjectDto::getScore)
                            .average()
                            .orElse(0)
            );
        }

        private String buildReason(PortfolioScenario scenario,
                                   Set<SimulationSkillDto> recommendedSkills,
                                   Set<SimulatedProjectDto> recommendedProjects) {

            String topSkills = recommendedSkills.stream()
                    .limit(3)
                    .map(SimulationSkillDto::getName)
                    .collect(Collectors.joining(", "));

            long relevantProjectsCount = recommendedProjects.stream()
                    .filter(project -> project.getScore() > 0)
                    .count();

            if (topSkills.isBlank()) {
                topSkills = "relevant skills";
            }

            return "This portfolio is well suited for \"" + scenario.getLabel()
                    + "\" thanks to " + topSkills
                    + " and " + relevantProjectsCount + " relevant project(s).";
        }

        private PortfolioScenarioDto mapScenarioToDto(PortfolioScenario scenario) {
            return PortfolioScenarioDto.builder()
                    .scenarioKey(scenario.getScenarioKey())
                    .label(scenario.getLabel())
                    .description(scenario.getDescription())
                    .build();
        }

        private String normalize(String value) {
            return value.trim().toLowerCase();
        }
    }

