package jihen.portfolio.services;

import jakarta.transaction.Transactional;
import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.Skill;
import jihen.portfolio.entities.User;
import jihen.portfolio.enums.PortfolioVisibility;
import jihen.portfolio.repositories.PortfolioRepository;
import jihen.portfolio.repositories.SkillRepository;
import jihen.portfolio.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jihen.portfolio.dto.PortfolioDNADto;
import jihen.portfolio.dtos.SkillNodeDto;
import java.util.*;
import java.util.stream.Collectors;

@Service
    public class PortfolioService {

        @Autowired
        private PortfolioRepository portfolioRepository;

        @Autowired
        private SkillRepository skillRepository;

        @Autowired
        private UserRepository userRepository;
        /**
         * Créer un nouveau portfolio
         */
        @Transactional
        public Portfolio createPortfolio(Long userId, Portfolio portfolio) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (portfolioRepository.existsByUserId(userId)) {
                throw new RuntimeException("Cet utilisateur a déjà un portfolio");
            }

            portfolio.setUser(user);
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

        public jihen.portfolio.dto.PortfolioDNADto getPortfolioDNA(Long portfolioId) {
            // 1. Récupérer le portfolio
            Portfolio portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

            // 2. Récupérer les skills du portfolio
            Set<Skill> skills = portfolio.getSkills();

            // 3. Construire l'arbre des compétences
            List<jihen.portfolio.dtos.SkillNodeDto> skillNodes = new ArrayList<>();

            for (Skill skill : skills) {
                List<jihen.portfolio.dtos.SkillNodeDto> subSkills = findRelatedSkills(skill);

                jihen.portfolio.dtos.SkillNodeDto node = new jihen.portfolio.dtos.SkillNodeDto(
                        skill.getId(),
                        skill.getName(),
                        skill.getTypecategory().toString(),
                        skill.getIsTrendy(),
                        subSkills
                );
                skillNodes.add(node);
            }

            // 4. Retourner le DNA
            return new jihen.portfolio.dto.PortfolioDNADto(portfolioId, portfolio.getTitle(), skillNodes);
        }

        private List<jihen.portfolio.dtos.SkillNodeDto> findRelatedSkills(Skill skill) {
            // Chercher des skills liés par catégorie ou par nom
            String skillName = skill.getName().toLowerCase();
            String category = skill.getTypecategory().toString();

            // Exemples de correspondances
            Map<String, List<String>> relatedMap = new HashMap<>();
            relatedMap.put("java", Arrays.asList("Spring Boot", "Maven", "Hibernate", "JUnit"));
            relatedMap.put("react", Arrays.asList("Redux", "React Router", "Next.js", "Tailwind"));
            relatedMap.put("python", Arrays.asList("Pandas", "NumPy", "Django", "Flask"));
            relatedMap.put("spring", Arrays.asList("Spring Security", "Spring Cloud", "Spring Data"));
            relatedMap.put("angular", Arrays.asList("RxJS", "TypeScript", "NgRx", "Angular Material"));

            List<String> relatedNames = relatedMap.getOrDefault(skillName, new ArrayList<>());

            // Convertir en SkillNodeDto
            return relatedNames.stream()
                    .map(name -> new jihen.portfolio.dtos.SkillNodeDto(null, name, category, false, new ArrayList<>()))
                    .collect(Collectors.toList());
        }
    }

