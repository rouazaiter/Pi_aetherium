package skillhub.portfolio_skillhub.services;


import skillhub.portfolio_skillhub.entities.Portfolio;
import skillhub.portfolio_skillhub.entities.PortfolioProject;
import skillhub.portfolio_skillhub.repositories.PortfolioProjectRepository;
import skillhub.portfolio_skillhub.repositories.PortfolioRepository;
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
    public void pinProject(Long projectId) {
        projectRepository.updatePinnedStatus(projectId, true);
    }

    /**
     * Désépingler un projet
     */
    public void unpinProject(Long projectId) {
        projectRepository.updatePinnedStatus(projectId, false);
    }

    /**
     * Récupérer les projets épinglés d'un portfolio
     */
    public List<PortfolioProject> getPinnedProjects(Long portfolioId) {
        return projectRepository.findByPortfolioIdAndIsPinnedTrue(portfolioId);
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

