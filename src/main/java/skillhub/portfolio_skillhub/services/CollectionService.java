
package skillhub.portfolio_skillhub.services;

import skillhub.portfolio_skillhub.entities.Collection;
import skillhub.portfolio_skillhub.entities.Portfolio;
import skillhub.portfolio_skillhub.entities.PortfolioProject;
import skillhub.portfolio_skillhub.repositories.CollectionRepository;
import skillhub.portfolio_skillhub.repositories.PortfolioRepository;
import skillhub.portfolio_skillhub.repositories.PortfolioProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioProjectRepository projectRepository;

    // ========== CRUD ==========

    /**
     * Créer une nouvelle collection dans un portfolio
     */
    public Collection createCollection(Long portfolioId, Collection collection) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));

        collection.setPortfolio(portfolio);
        return collectionRepository.save(collection);
    }

    /**
     * Récupérer toutes les collections d'un portfolio
     */
    public List<Collection> getCollectionsByPortfolioId(Long portfolioId) {
        return collectionRepository.findByPortfolioId(portfolioId);
    }

    /**
     * Récupérer une collection par son ID
     */
    public Optional<Collection> getCollectionById(Long id) {
        return collectionRepository.findById(id);
    }

    /**
     * Mettre à jour une collection
     */
    public Collection updateCollection(Long id, Collection collectionDetails) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée"));

        collection.setName(collectionDetails.getName());
        collection.setDescription(collectionDetails.getDescription());

        return collectionRepository.save(collection);
    }

    /**
     * Supprimer une collection
     */
    public void deleteCollection(Long id) {
        collectionRepository.deleteById(id);
    }

    /**
     * Supprimer toutes les collections d'un portfolio
     */
    public void deleteCollectionsByPortfolioId(Long portfolioId) {
        collectionRepository.deleteByPortfolioId(portfolioId);
    }

    // ========== GESTION DES PROJETS DANS LA COLLECTION ==========

    /**
     * Ajouter un projet à une collection
     */
    public Collection addProjectToCollection(Long collectionId, Long projectId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée"));

        PortfolioProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        collection.addProject(project);
        return collectionRepository.save(collection);
    }

    /**
     * Supprimer un projet d'une collection
     */
    public Collection removeProjectFromCollection(Long collectionId, Long projectId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée"));

        PortfolioProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        collection.removeProject(project);
        return collectionRepository.save(collection);
    }

    /**
     * Récupérer tous les projets d'une collection
     */
    public List<PortfolioProject> getProjectsByCollectionId(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée"));
        return collection.getProjects();
    }

    // ========== RECHERCHE ==========

    /**
     * Rechercher des collections par nom
     */
    public List<Collection> searchCollectionsByName(String name) {
        return collectionRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Rechercher des collections dans un portfolio par nom
     */
    public List<Collection> searchCollectionsInPortfolio(Long portfolioId, String name) {
        return collectionRepository.findByPortfolioIdAndNameContainingIgnoreCase(portfolioId, name);
    }

    /**
     * Vérifier si une collection existe
     */
    public boolean existsByPortfolioIdAndName(Long portfolioId, String name) {
        return collectionRepository.existsByPortfolioIdAndName(portfolioId, name);
    }
}