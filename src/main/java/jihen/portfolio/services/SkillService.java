package jihen.portfolio.services;

import jihen.portfolio.entities.Portfolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jihen.portfolio.entities.Skill;
import jihen.portfolio.enums.SkillCategory;
import jihen.portfolio.repositories.SkillRepository;

import java.util.List;
import java.util.Optional;


    @Service
    public class SkillService {

        @Autowired
        private SkillRepository skillRepository;




        public Skill createSkill(Skill skill) {
            if (skillRepository.existsByName(skill.getName())) {
                throw new RuntimeException("Ce skill existe déjà");
            }
            return skillRepository.save(skill);
        }


        public List<Skill> getAllSkills() {
            return skillRepository.findAll();
        }


        public Optional<Skill> getSkillById(Long id) {
            return skillRepository.findById(id);
        }


        public Optional<Skill> getSkillByName(String name) {
            return skillRepository.findByName(name);
        }


        public Skill updateSkill(Long id, Skill skillDetails) {
            Skill skill = skillRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Skill non trouvé"));

            skill.setName(skillDetails.getName());
            skill.setTypecategory(skillDetails.getTypecategory());
            skill.setDescription(skillDetails.getDescription());

            return skillRepository.save(skill);
        }

        public List<Portfolio> getPortfoliosBySkillId(Long skillId) {
            // Vérifie que le skill existe
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill non trouvé avec l'id: " + skillId));

            return skillRepository.findPortfoliosBySkillId(skillId);
        }

        public void deleteSkill(Long id) {
            skillRepository.deleteById(id);
        }


        public List<Skill> getSkillsByCategory(SkillCategory category) {
            return skillRepository.findByTypecategory(category);
        }


        public List<Skill> getTrendySkills() {
            return skillRepository.findByIsTrendyTrue();
        }


        public List<Skill> getMostSearchedSkills() {
            return skillRepository.findAllByOrderBySearchCountDesc();
        }


        public List<Skill> searchSkillsByName(String name) {
            return skillRepository.findByNameContainingIgnoreCase(name);
        }



        public void incrementSearchCount(Long skillId) {
            skillRepository.incrementSearchCount(skillId);
            skillRepository.markTrendySkills(100);
        }

        public void markAsTrendy(Long skillId) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill non trouvé"));
            skill.markAsTrendy();
            skillRepository.save(skill);
        }


        public void resetAllTrendySkills() {
            skillRepository.unmarkAllTrendySkills();
        }
        public List<Skill> getTrendingSkills() {
            return skillRepository.findTop10ByOrderBySearchCountDesc();
        }
    }


