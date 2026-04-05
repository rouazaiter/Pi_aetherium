package skillhub.portfolio_skillhub.services;

import jakarta.transaction.Transactional;
import skillhub.portfolio_skillhub.entities.Portfolio;
import skillhub.portfolio_skillhub.entities.Skill;
import skillhub.portfolio_skillhub.enums.PortfolioVisibility;
import skillhub.portfolio_skillhub.repositories.PortfolioRepository;
import skillhub.portfolio_skillhub.repositories.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

    @Service
    public class PortfolioService {

        @Autowired
        private PortfolioRepository portfolioRepository;

        @Autowired
        private SkillRepository skillRepository;


        /**
         * Créer un nouveau portfolio
         */
        @Transactional
        public Portfolio createPortfolio(Portfolio portfolio) {
            if (portfolioRepository.existsByUserId(portfolio.getUserId())) {
                throw new RuntimeException("Cet utilisateur a déjà un portfolio");
            }
            return portfolioRepository.save(portfolio);
        }

        /**
         * Récupérer tous les portfolios
         */
        public List<Portfolio> getAllPortfolios() {
            return portfolioRepository.findAll();
        }

        /**
         * Récupérer un portfolio par son ID
         */
        @Transactional
        public Optional<Portfolio> getPortfolioById(Long id) {
            return portfolioRepository.findById(id);
        }

        /**
         * Récupérer le portfolio d'un utilisateur
         */
        @Transactional
        public Optional<Portfolio> getPortfolioByUserId(Long userId) {
            return portfolioRepository.findByUserId(userId);
        }

        /**
         * Mettre à jour un portfolio
         */
        @Transactional
        public Portfolio updatePortfolio(Long id, Portfolio portfolioDetails) {
            Portfolio portfolio = portfolioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

            portfolio.setTitle(portfolioDetails.getTitle());
            portfolio.setBio(portfolioDetails.getBio());
            portfolio.setProfilePicture(portfolioDetails.getProfilePicture());
            portfolio.setCoverImage(portfolioDetails.getCoverImage());
            portfolio.setLocation(portfolioDetails.getLocation());
            portfolio.setVisibility(portfolioDetails.getVisibility());

            return portfolioRepository.save(portfolio);
        }

        /**
         * Supprimer un portfolio
         */
        @Transactional
        public void deletePortfolio(Long id) {
            portfolioRepository.deleteById(id);
        }

        // ========== GESTION DES SKILLS ==========

        /**
         * Ajouter un skill au portfolio
         */
        @Transactional
        public Portfolio addSkillToPortfolio(Long portfolioId, Long skillId) {
            Portfolio portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill non trouvé"));

            portfolio.addSkill(skill);
            return portfolioRepository.save(portfolio);
        }

        /**
         * Supprimer un skill du portfolio
         */
        @Transactional
        public Portfolio removeSkillFromPortfolio(Long portfolioId, Long skillId) {
            Portfolio portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill non trouvé"));

            portfolio.removeSkill(skill);
            return portfolioRepository.save(portfolio);
        }

        /**
         * Récupérer tous les skills d'un portfolio
         */
        @Transactional
        public Set<Skill> getSkillsByPortfolioId(Long portfolioId) {
            Portfolio portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));
            return portfolio.getSkills();
        }

        // ========== GESTION DE LA VISIBILITÉ ==========

        /**
         * Changer la visibilité du portfolio
         */
        @Transactional
        public Portfolio changeVisibility(Long portfolioId, PortfolioVisibility visibility) {
            portfolioRepository.updateVisibility(portfolioId, visibility);
            return portfolioRepository.findById(portfolioId).orElse(null);
        }

        /**
         * Récupérer tous les portfolios publics
         */
        @Transactional
        public List<Portfolio> getPublicPortfolios() {
            return portfolioRepository.findByVisibility(PortfolioVisibility.PUBLIC);
        }

        // ========== STATISTIQUES ==========

        /**
         * Incrémenter le compteur de vues
         */
        @Transactional
        public void incrementViews(Long portfolioId) {
            portfolioRepository.incrementViewCount(portfolioId);
        }

        /**
         * Récupérer le nombre total de vues de tous les portfolios
         */
        @Transactional
        public Long getTotalViews() {
            return portfolioRepository.getTotalViewsSum();
        }

        // ========== RECHERCHE ==========

        /**
         * Rechercher des portfolios par mot clé
         */
        @Transactional
        public List<Portfolio> searchPortfolios(String keyword) {
            return portfolioRepository.searchByKeyword(keyword);
        }

        /**
         * Rechercher des portfolios par localisation
         */
        @Transactional
        public List<Portfolio> searchByLocation(String location) {
            return portfolioRepository.findByLocationContainingIgnoreCase(location);
        }
    }

