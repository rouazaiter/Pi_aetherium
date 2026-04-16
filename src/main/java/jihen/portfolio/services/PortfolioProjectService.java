package jihen.portfolio.services;


import jakarta.transaction.Transactional;
import jihen.portfolio.dtos.ProjectImpactDto;
import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.PortfolioProject;
import jihen.portfolio.repositories.PortfolioProjectRepository;
import jihen.portfolio.repositories.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PortfolioProjectService {

    @Autowired
    private PortfolioProjectRepository projectRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    // ========== CRUD ==========

    /**
     * Créer un nouveau projet dans un portfolio
     */
    public PortfolioProject createProject(Long portfolioId, PortfolioProject project) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

        project.setPortfolio(portfolio);
        return projectRepository.save(project);
    }

    /**
     * Récupérer tous les projets d'un portfolio
     */
    public List<PortfolioProject> getProjectsByPortfolioId(Long portfolioId) {
        return projectRepository.findByPortfolioId(portfolioId);
    }

    /**
     * Récupérer un projet par son ID
     */
    public Optional<PortfolioProject> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * Mettre à jour un projet
     */
    public PortfolioProject updateProject(Long id, PortfolioProject projectDetails) {
        PortfolioProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        project.setTitle(projectDetails.getTitle());
        project.setDescription(projectDetails.getDescription());
        project.setProjectUrl(projectDetails.getProjectUrl());

        return projectRepository.save(project);
    }

    /**
     * Supprimer un projet
     */
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    // ========== PROJETS ÉPINGLÉS ==========

    /**
     * Épingler un projet
     */
    public ProjectImpactDto calculateImpact(Long projectId) {
        PortfolioProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // Valeurs de référence
        int maxViews = 1000;
        int maxLikes = 150;

        // Récupérer les valeurs (avec gestion des null)
        int views = project.getViews() != null ? project.getViews() : 0;
        int likes = project.getLikes() != null ? project.getLikes() : 0;

        // Calcul des scores (plafonné à 100%)
        int viewsScore = Math.min(views * 40 / maxViews, 40);
        int likesScore = Math.min(likes * 30 / maxLikes, 30);
        int clicksScore = (project.getProjectUrl() != null && !project.getProjectUrl().isEmpty()) ? 20 : 0;
        int pinnedBonus = (project.getPinned() != null && project.getPinned()) ? 10 : 0;

        int totalScore = viewsScore + likesScore + clicksScore + pinnedBonus;
        String level = getLevel(totalScore);

        return ProjectImpactDto.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .impactScore(totalScore)
                .level(level)
                .views(views)
                .likes(likes)
                .hasUrl(project.getProjectUrl() != null && !project.getProjectUrl().isEmpty())
                .isPinned(project.getPinned() != null && project.getPinned())
                .build();
    }

    private String getLevel(int score) {
        if (score >= 86) return "EXCEPTIONNEL";
        if (score >= 71) return "ÉLEVÉ";
        if (score >= 41) return "MOYEN";
        return "FAIBLE";
    }
    @Transactional
    public void pinProject(Long projectId) {
        projectRepository.updatePinnedStatus(projectId, true);
    }

    /**
     * Désépingler un projet
     */
    @Transactional
    public void unpinProject(Long projectId) {
        projectRepository.updatePinnedStatus(projectId, false);
    }

    /**
     * Récupérer les projets épinglés d'un portfolio
     */
    public List<PortfolioProject> getPinnedProjects(Long portfolioId) {
        return projectRepository.findByPortfolioIdAndPinnedTrue(portfolioId);
    }

    // ========== STATISTIQUES ==========

    /**
     * Incrémenter les vues d'un projet
     */
    public void incrementViews(Long projectId) {
        projectRepository.incrementViews(projectId);
    }

    /**
     * Incrémenter les likes d'un projet
     */
    public void incrementLikes(Long projectId) {
        projectRepository.incrementLikes(projectId);
    }

    /**
     * Récupérer les projets les plus vus
     */
    public List<PortfolioProject> getMostViewedProjects(Long portfolioId) {
        return projectRepository.findByPortfolioIdOrderByViewsDesc(portfolioId);
    }

    /**
     * Récupérer les projets les plus likés
     */
    public List<PortfolioProject> getMostLikedProjects(Long portfolioId) {
        return projectRepository.findByPortfolioIdOrderByLikesDesc(portfolioId);
    }

    /**
     * Récupérer les projets les plus récents
     */
    public List<PortfolioProject> getRecentProjects(Long portfolioId) {
        return projectRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId);
    }
}

