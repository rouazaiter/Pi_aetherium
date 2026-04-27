package com.education.platform.services.implementations.portfolio;

import com.education.platform.common.ApiException;
import com.education.platform.dto.portfolio.CreatePortfolioCollectionRequest;
import com.education.platform.dto.portfolio.PortfolioCollectionResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioCollectionRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioCollection;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.portfolio.PortfolioCollectionRepository;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.portfolio.PortfolioCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortfolioCollectionServiceImpl implements PortfolioCollectionService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioCollectionRepository portfolioCollectionRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final PortfolioMapper portfolioMapper;

    public PortfolioCollectionServiceImpl(
            PortfolioRepository portfolioRepository,
            PortfolioCollectionRepository portfolioCollectionRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            PortfolioMapper portfolioMapper) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioCollectionRepository = portfolioCollectionRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.portfolioMapper = portfolioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioCollectionResponse> listForUser(User user) {
        Portfolio portfolio = requirePortfolio(user);
        return portfolioCollectionRepository.findByPortfolio_IdOrderByCreatedAtDesc(portfolio.getId()).stream()
                .map(portfolioMapper::toCollectionResponse)
                .toList();
    }

    @Override
    @Transactional
    public PortfolioCollectionResponse createForUser(User user, CreatePortfolioCollectionRequest request) {
        Portfolio portfolio = requirePortfolio(user);
        Visibility visibility = request.getVisibility() == null ? Visibility.PRIVATE : request.getVisibility();
        validateChildVisibility(portfolio.getVisibility(), visibility);

        PortfolioCollection collection = PortfolioCollection.builder()
                .name(request.getName())
                .description(request.getDescription())
                .visibility(visibility)
                .build();

        portfolio.addCollection(collection);
        PortfolioCollection saved = portfolioCollectionRepository.save(collection);
        return portfolioMapper.toCollectionResponse(saved);
    }

    @Override
    @Transactional
    public PortfolioCollectionResponse updateForUser(User user, Long collectionId, UpdatePortfolioCollectionRequest request) {
        PortfolioCollection collection = requireOwnedCollection(user, collectionId);

        if (request.getName() != null) {
            collection.setName(request.getName());
        }
        if (request.getDescription() != null) {
            collection.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            validateChildVisibility(collection.getPortfolio().getVisibility(), request.getVisibility());
            collection.setVisibility(request.getVisibility());
        }

        return portfolioMapper.toCollectionResponse(collection);
    }

    @Override
    @Transactional
    public void deleteForUser(User user, Long collectionId) {
        PortfolioCollection collection = requireOwnedCollection(user, collectionId);
        portfolioCollectionRepository.delete(collection);
    }

    @Override
    @Transactional
    public PortfolioCollectionResponse addProjectToCollection(User user, Long collectionId, Long projectId) {
        PortfolioCollection collection = requireOwnedCollection(user, collectionId);
        PortfolioProject project = requireOwnedProject(user, projectId);
        ensureSamePortfolio(collection, project);

        boolean exists = collection.getCollectionProjects().stream()
                .anyMatch(link -> link.getProject() != null && projectId.equals(link.getProject().getId()));
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "Le projet appartient deja a cette collection");
        }

        collection.addProject(project);
        return portfolioMapper.toCollectionResponse(collection);
    }

    @Override
    @Transactional
    public PortfolioCollectionResponse removeProjectFromCollection(User user, Long collectionId, Long projectId) {
        PortfolioCollection collection = requireOwnedCollection(user, collectionId);
        PortfolioProject project = requireOwnedProject(user, projectId);
        ensureSamePortfolio(collection, project);

        boolean exists = collection.getCollectionProjects().stream()
                .anyMatch(link -> link.getProject() != null && projectId.equals(link.getProject().getId()));
        if (!exists) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Ce projet n'appartient pas a la collection");
        }

        collection.removeProject(project);
        return portfolioMapper.toCollectionResponse(collection);
    }

    private Portfolio requirePortfolio(User user) {
        return portfolioRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Portfolio introuvable"));
    }

    private PortfolioCollection requireOwnedCollection(User user, Long collectionId) {
        return portfolioCollectionRepository.findByIdAndPortfolio_User_Id(collectionId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Collection introuvable"));
    }

    private PortfolioProject requireOwnedProject(User user, Long projectId) {
        return portfolioProjectRepository.findByIdAndPortfolio_User_Id(projectId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Projet introuvable"));
    }

    private void ensureSamePortfolio(PortfolioCollection collection, PortfolioProject project) {
        Long collectionPortfolioId = collection.getPortfolio() == null ? null : collection.getPortfolio().getId();
        Long projectPortfolioId = project.getPortfolio() == null ? null : project.getPortfolio().getId();
        if (collectionPortfolioId == null || !collectionPortfolioId.equals(projectPortfolioId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le projet et la collection doivent appartenir au meme portfolio");
        }
    }

    private void validateChildVisibility(Visibility parentVisibility, Visibility childVisibility) {
        if (visibilityRank(childVisibility) > visibilityRank(parentVisibility)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La visibilite de la collection depasse celle du portfolio");
        }
    }

    private int visibilityRank(Visibility visibility) {
        if (visibility == null) {
            return 0;
        }
        return switch (visibility) {
            case PRIVATE -> 0;
            case FRIENDS_ONLY -> 1;
            case PUBLIC -> 2;
        };
    }
}
