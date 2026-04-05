package skillhub.portfolio_skillhub.controllers;

import skillhub.portfolio_skillhub.entities.Collection;
import skillhub.portfolio_skillhub.entities.PortfolioProject;
import skillhub.portfolio_skillhub.services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    // ========== CRUD ==========

    @PostMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Collection> createCollection(@PathVariable Long portfolioId, @RequestBody Collection collection) {
        Collection created = collectionService.createCollection(portfolioId, collection);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<Collection>> getCollectionsByPortfolioId(@PathVariable Long portfolioId) {
        List<Collection> collections = collectionService.getCollectionsByPortfolioId(portfolioId);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Collection> getCollectionById(@PathVariable Long id) {
        Optional<Collection> collection = collectionService.getCollectionById(id);
        return collection.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> updateCollection(@PathVariable Long id, @RequestBody Collection collection) {
        Collection updated = collectionService.updateCollection(id, collection);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Void> deleteCollectionsByPortfolioId(@PathVariable Long portfolioId) {
        collectionService.deleteCollectionsByPortfolioId(portfolioId);
        return ResponseEntity.noContent().build();
    }

    // ========== GESTION DES PROJETS ==========

    @PostMapping("/{collectionId}/projects/{projectId}")
    public ResponseEntity<Collection> addProjectToCollection(@PathVariable Long collectionId, @PathVariable Long projectId) {
        Collection collection = collectionService.addProjectToCollection(collectionId, projectId);
        return ResponseEntity.ok(collection);
    }

    @DeleteMapping("/{collectionId}/projects/{projectId}")
    public ResponseEntity<Collection> removeProjectFromCollection(@PathVariable Long collectionId, @PathVariable Long projectId) {
        Collection collection = collectionService.removeProjectFromCollection(collectionId, projectId);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{collectionId}/projects")
    public ResponseEntity<List<PortfolioProject>> getProjectsByCollectionId(@PathVariable Long collectionId) {
        List<PortfolioProject> projects = collectionService.getProjectsByCollectionId(collectionId);
        return ResponseEntity.ok(projects);
    }

    // ========== RECHERCHE ==========

    @GetMapping("/search")
    public ResponseEntity<List<Collection>> searchCollections(@RequestParam String name) {
        List<Collection> collections = collectionService.searchCollectionsByName(name);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/portfolio/{portfolioId}/search")
    public ResponseEntity<List<Collection>> searchCollectionsInPortfolio(@PathVariable Long portfolioId, @RequestParam String name) {
        List<Collection> collections = collectionService.searchCollectionsInPortfolio(portfolioId, name);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/portfolio/{portfolioId}/exists")
    public ResponseEntity<Boolean> existsByName(@PathVariable Long portfolioId, @RequestParam String name) {
        boolean exists = collectionService.existsByPortfolioIdAndName(portfolioId, name);
        return ResponseEntity.ok(exists);
    }
}

