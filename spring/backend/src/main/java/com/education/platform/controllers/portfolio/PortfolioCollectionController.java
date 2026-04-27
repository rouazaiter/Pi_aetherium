package com.education.platform.controllers.portfolio;

import com.education.platform.dto.portfolio.CreatePortfolioCollectionRequest;
import com.education.platform.dto.portfolio.PortfolioCollectionResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioCollectionRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.PortfolioCollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/me/collections")
public class PortfolioCollectionController {

    private final CurrentUserService currentUserService;
    private final PortfolioCollectionService portfolioCollectionService;

    public PortfolioCollectionController(
            CurrentUserService currentUserService,
            PortfolioCollectionService portfolioCollectionService) {
        this.currentUserService = currentUserService;
        this.portfolioCollectionService = portfolioCollectionService;
    }

    @GetMapping
    public List<PortfolioCollectionResponse> myCollections() {
        return portfolioCollectionService.listForUser(currentUserService.getCurrentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioCollectionResponse createCollection(@Valid @RequestBody CreatePortfolioCollectionRequest request) {
        return portfolioCollectionService.createForUser(currentUserService.getCurrentUser(), request);
    }

    @PutMapping("/{collectionId}")
    public PortfolioCollectionResponse updateCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody UpdatePortfolioCollectionRequest request) {
        return portfolioCollectionService.updateForUser(currentUserService.getCurrentUser(), collectionId, request);
    }

    @DeleteMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(@PathVariable Long collectionId) {
        portfolioCollectionService.deleteForUser(currentUserService.getCurrentUser(), collectionId);
    }

    @PostMapping("/{collectionId}/projects/{projectId}")
    public PortfolioCollectionResponse addProjectToCollection(@PathVariable Long collectionId, @PathVariable Long projectId) {
        return portfolioCollectionService.addProjectToCollection(currentUserService.getCurrentUser(), collectionId, projectId);
    }

    @DeleteMapping("/{collectionId}/projects/{projectId}")
    public PortfolioCollectionResponse removeProjectFromCollection(@PathVariable Long collectionId, @PathVariable Long projectId) {
        return portfolioCollectionService.removeProjectFromCollection(currentUserService.getCurrentUser(), collectionId, projectId);
    }
}
