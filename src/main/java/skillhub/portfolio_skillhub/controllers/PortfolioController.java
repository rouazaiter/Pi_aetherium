package skillhub.portfolio_skillhub.controllers;


import skillhub.portfolio_skillhub.entities.Portfolio;
import skillhub.portfolio_skillhub.entities.Skill;
import skillhub.portfolio_skillhub.enums.PortfolioVisibility;
import skillhub.portfolio_skillhub.services.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

    @RestController
    @RequestMapping("/api/portfolios")
    public class PortfolioController {

        @Autowired
        private PortfolioService portfolioService;


        @PostMapping
        public ResponseEntity<Portfolio> createPortfolio(@RequestBody Portfolio portfolio) {
            Portfolio created = portfolioService.createPortfolio(portfolio);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        }

        /**
         * Récupérer tous les portfolios
         * GET /api/portfolios
         */
        @GetMapping
        public ResponseEntity<List<Portfolio>> getAllPortfolios() {
            List<Portfolio> portfolios = portfolioService.getAllPortfolios();
            return ResponseEntity.ok(portfolios);
        }

        /**
         * Récupérer un portfolio par son ID
         * GET /api/portfolios/{id}
         */
        @GetMapping("/{id}")
        public ResponseEntity<Portfolio> getPortfolioById(@PathVariable Long id) {
            Optional<Portfolio> portfolio = portfolioService.getPortfolioById(id);
            return portfolio.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Récupérer le portfolio d'un utilisateur
         * GET /api/portfolios/user/{userId}
         */
        @GetMapping("/user/{userId}")
        public ResponseEntity<Portfolio> getPortfolioByUserId(@PathVariable Long userId) {
            Optional<Portfolio> portfolio = portfolioService.getPortfolioByUserId(userId);
            return portfolio.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }


        @PutMapping("/{id}")
        public ResponseEntity<Portfolio> updatePortfolio(@PathVariable Long id, @RequestBody Portfolio portfolio) {
            Portfolio updated = portfolioService.updatePortfolio(id, portfolio);
            return ResponseEntity.ok(updated);
        }


        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
            portfolioService.deletePortfolio(id);
            return ResponseEntity.noContent().build();
        }



        @PostMapping("/{portfolioId}/skills/{skillId}")
        public ResponseEntity<Portfolio> addSkillToPortfolio(@PathVariable Long portfolioId, @PathVariable Long skillId) {
            Portfolio portfolio = portfolioService.addSkillToPortfolio(portfolioId, skillId);
            return ResponseEntity.ok(portfolio);
        }

        @DeleteMapping("/{portfolioId}/skills/{skillId}")
        public ResponseEntity<Portfolio> removeSkillFromPortfolio(@PathVariable Long portfolioId, @PathVariable Long skillId) {
            Portfolio portfolio = portfolioService.removeSkillFromPortfolio(portfolioId, skillId);
            return ResponseEntity.ok(portfolio);
        }

        /**
         * Récupérer tous les skills d'un portfolio
         * GET /api/portfolios/{portfolioId}/skills
         */
        @GetMapping("/{portfolioId}/skills")
        public ResponseEntity<Set<Skill>> getSkillsByPortfolio(@PathVariable Long portfolioId) {
            Set<Skill> skills = portfolioService.getSkillsByPortfolioId(portfolioId);
            return ResponseEntity.ok(skills);
        }

        // ========== GESTION DE LA VISIBILITÉ ==========

        /**
         * Changer la visibilité du portfolio
         * PATCH /api/portfolios/{id}/visibility?visibility=PUBLIC
         */
        @PatchMapping("/{id}/visibility")
        public ResponseEntity<Portfolio> changeVisibility(@PathVariable Long id, @RequestParam PortfolioVisibility visibility) {
            Portfolio portfolio = portfolioService.changeVisibility(id, visibility);
            return ResponseEntity.ok(portfolio);
        }

        /**
         * Récupérer tous les portfolios publics
         * GET /api/portfolios/public/all
         */
        @GetMapping("/public/all")
        public ResponseEntity<List<Portfolio>> getPublicPortfolios() {
            List<Portfolio> portfolios = portfolioService.getPublicPortfolios();
            return ResponseEntity.ok(portfolios);
        }

        // ========== STATISTIQUES ==========

        /**
         * Incrémenter les vues d'un portfolio
         * POST /api/portfolios/{id}/increment-views
         */
        @PostMapping("/{id}/increment-views")
        public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
            portfolioService.incrementViews(id);
            return ResponseEntity.ok().build();
        }

        /**
         * Récupérer le nombre total de vues
         * GET /api/portfolios/stats/total-views
         */
        @GetMapping("/stats/total-views")
        public ResponseEntity<Long> getTotalViews() {
            Long totalViews = portfolioService.getTotalViews();
            return ResponseEntity.ok(totalViews);
        }

        // ========== RECHERCHE ==========

        /**
         * Rechercher des portfolios par mot clé
         * GET /api/portfolios/search?keyword=java
         */
        @GetMapping("/search")
        public ResponseEntity<List<Portfolio>> searchPortfolios(@RequestParam String keyword) {
            List<Portfolio> portfolios = portfolioService.searchPortfolios(keyword);
            return ResponseEntity.ok(portfolios);
        }

        /**
         * Rechercher des portfolios par localisation
         * GET /api/portfolios/search/by-location?location=Tunis
         */
        @GetMapping("/search/by-location")
        public ResponseEntity<List<Portfolio>> searchByLocation(@RequestParam String location) {
            List<Portfolio> portfolios = portfolioService.searchByLocation(location);
            return ResponseEntity.ok(portfolios);
        }
    }

