package jihen.portfolio.services;


import jihen.portfolio.dtos.SkillGapDto;
import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

    @Service
    public class SkillGapService {

        @Autowired
        private PortfolioService portfolioService;

        @Autowired
        private SkillService skillService;

        public SkillGapDto analyzeGap(Long portfolioId) {
            // 1. Récupérer le portfolio
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId)
                    .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

            // 2. Récupérer les skills du portfolio
            Set<Skill> portfolioSkills = portfolio.getSkills();
            Set<String> portfolioSkillNames = portfolioSkills.stream()
                    .map(Skill::getName)
                    .collect(Collectors.toSet());

            // 3. Récupérer les skills tendances (top 10)
            List<Skill> trendingSkills = skillService.getTrendingSkills();
            if (trendingSkills.size() > 10) {
                trendingSkills = trendingSkills.subList(0, 10);
            }

            // 4. Identifier les skills manquants
            List<SkillGapDto.MissingSkillDto> missingSkills = new ArrayList<>();
            for (Skill skill : trendingSkills) {
                if (!portfolioSkillNames.contains(skill.getName())) {
                    missingSkills.add(SkillGapDto.MissingSkillDto.builder()
                            .name(skill.getName())
                            .category(skill.getTypecategory().toString())
                            .trendingScore(skill.getSearchCount())
                            .potentialGain(calculatePotentialGain(skill.getSearchCount()))
                            .reason(getReason(skill.getTypecategory().toString()))
                            .build());
                }
            }

            // 5. Calculer le score de complétude
            int totalTrendingSkills = trendingSkills.size();
            int ownedSkills = (int) trendingSkills.stream()
                    .filter(s -> portfolioSkillNames.contains(s.getName()))
                    .count();
            int completenessScore = (totalTrendingSkills > 0) ? (ownedSkills * 100) / totalTrendingSkills : 0;

            // 6. Générer les recommandations
            List<String> recommendations = missingSkills.stream()
                    .map(m -> "Ajoutez " + m.getName() + " pour " + m.getPotentialGain())
                    .collect(Collectors.toList());

            // 7. Skills actuels du portfolio
            List<SkillGapDto.CurrentSkillDto> currentSkills = portfolioSkills.stream()
                    .map(s -> SkillGapDto.CurrentSkillDto.builder()
                            .name(s.getName())
                            .category(s.getTypecategory().toString())
                            .proficiency(80) // Valeur par défaut
                            .build())
                    .collect(Collectors.toList());

            // 8. Construire la réponse
            return SkillGapDto.builder()
                    .portfolioId(portfolio.getId())
                    .portfolioTitle(portfolio.getTitle())
                    .completenessScore(completenessScore)
                    .currentSkills(currentSkills)
                    .missingSkills(missingSkills)
                    .recommendations(recommendations)
                    .build();
        }

        private String calculatePotentialGain(Integer searchCount) {
            if (searchCount == null) return "+10% visibilité";
            if (searchCount > 1000) return "+50% visibilité";
            if (searchCount > 500) return "+35% visibilité";
            if (searchCount > 100) return "+20% visibilité";
            return "+10% visibilité";
        }

        private String getReason(String category) {
            switch (category) {
                case "BACKEND_DEVELOPMENT":
                    return "Très demandé par les recruteurs backend";
                case "FRONTEND_DEVELOPMENT":
                    return "Essentiel pour le développement frontend";
                case "DATA_ANALYSIS":
                    return "Compétence clé pour l'analyse de données";
                case "DEVOPS":
                    return "Recherché pour l'infrastructure cloud";
                case "UI_UX_DESIGN":
                    return "Très recherché pour le design d'interface";
                default:
                    return "Compétence tendance du marché";
            }
        }
    }

