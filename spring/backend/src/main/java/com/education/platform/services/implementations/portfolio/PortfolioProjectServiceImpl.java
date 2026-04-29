package com.education.platform.services.implementations.portfolio;

import com.education.platform.common.ApiException;
import com.education.platform.dto.portfolio.CreatePortfolioProjectRequest;
import com.education.platform.dto.portfolio.PortfolioProjectResponse;
import com.education.platform.dto.portfolio.ProjectMediaRequest;
import com.education.platform.dto.portfolio.UpdatePortfolioProjectRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.ProjectMedia;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.portfolio.PortfolioProjectService;
import com.education.platform.services.interfaces.portfolio.SkillCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PortfolioProjectServiceImpl implements PortfolioProjectService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final SkillCatalogService skillCatalogService;
    private final PortfolioMediaStorage portfolioMediaStorage;
    private final PortfolioMapper portfolioMapper;

    public PortfolioProjectServiceImpl(
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            SkillCatalogService skillCatalogService,
            PortfolioMediaStorage portfolioMediaStorage,
            PortfolioMapper portfolioMapper) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.skillCatalogService = skillCatalogService;
        this.portfolioMediaStorage = portfolioMediaStorage;
        this.portfolioMapper = portfolioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioProjectResponse> listForUser(User user) {
        Portfolio portfolio = requirePortfolio(user);
        return portfolioProjectRepository.findByPortfolio_IdOrderByCreatedAtDesc(portfolio.getId()).stream()
                .map(portfolioMapper::toProjectResponse)
                .toList();
    }

    @Override
    @Transactional
    public PortfolioProjectResponse createForUser(User user, CreatePortfolioProjectRequest request) {
        Portfolio portfolio = requirePortfolio(user);
        Visibility visibility = request.getVisibility() == null ? Visibility.PRIVATE : request.getVisibility();
        validateChildVisibility(portfolio.getVisibility(), visibility);

        PortfolioProject project = PortfolioProject.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .projectUrl(request.getProjectUrl())
                .pinned(Boolean.TRUE.equals(request.getPinned()))
                .visibility(visibility)
                .skills(new LinkedHashSet<>())
                .media(new ArrayList<>())
                .build();

        portfolio.addProject(project);
        Set<Skill> selectedSkills = skillCatalogService.requireSkillsByIds(request.getSkillIds());
        project.getSkills().addAll(selectedSkills);
        portfolio.getSkills().addAll(selectedSkills);
        replaceMedia(project, request.getMedia());

        PortfolioProject saved = portfolioProjectRepository.save(project);
        return portfolioMapper.toProjectResponse(saved);
    }

    @Override
    @Transactional
    public PortfolioProjectResponse updateForUser(User user, Long projectId, UpdatePortfolioProjectRequest request) {
        PortfolioProject project = requireOwnedProject(user, projectId);

        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getProjectUrl() != null) {
            project.setProjectUrl(request.getProjectUrl());
        }
        if (request.getPinned() != null) {
            project.setPinned(request.getPinned());
        }
        if (request.getVisibility() != null) {
            validateChildVisibility(project.getPortfolio().getVisibility(), request.getVisibility());
            project.setVisibility(request.getVisibility());
        }
        if (request.getSkillIds() != null) {
            Set<Skill> selectedSkills = skillCatalogService.requireSkillsByIds(request.getSkillIds());
            project.getSkills().clear();
            project.getSkills().addAll(selectedSkills);
            project.getPortfolio().getSkills().addAll(selectedSkills);
        }
        if (request.getMedia() != null) {
            replaceMedia(project, request.getMedia());
        }

        return portfolioMapper.toProjectResponse(project);
    }

    @Override
    @Transactional
    public void deleteForUser(User user, Long projectId) {
        PortfolioProject project = requireOwnedProject(user, projectId);
        deleteManagedMediaFiles(project.getMedia());
        portfolioProjectRepository.delete(project);
    }

    private Portfolio requirePortfolio(User user) {
        return portfolioRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Portfolio introuvable"));
    }

    private PortfolioProject requireOwnedProject(User user, Long projectId) {
        return portfolioProjectRepository.findByIdAndPortfolio_User_Id(projectId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Projet introuvable"));
    }

    private void replaceMedia(PortfolioProject project, List<ProjectMediaRequest> mediaRequests) {
        for (ProjectMedia media : new ArrayList<>(project.getMedia())) {
            portfolioMediaStorage.deleteIfManagedByUs(media.getMediaUrl());
            project.removeMedia(media);
        }
        if (mediaRequests == null) {
            return;
        }

        for (int i = 0; i < mediaRequests.size(); i++) {
            ProjectMediaRequest request = mediaRequests.get(i);
            if (request == null) {
                continue;
            }
            ProjectMedia media = ProjectMedia.builder()
                    .mediaUrl(request.getMediaUrl())
                    .mediaType(request.getMediaType())
                    .orderIndex(request.getOrderIndex() == null ? i : request.getOrderIndex())
                    .build();
            project.addMedia(media);
        }
    }

    private void deleteManagedMediaFiles(List<ProjectMedia> mediaItems) {
        for (ProjectMedia media : mediaItems) {
            if (media != null) {
                portfolioMediaStorage.deleteIfManagedByUs(media.getMediaUrl());
            }
        }
    }

    private void validateChildVisibility(Visibility parentVisibility, Visibility childVisibility) {
        if (visibilityRank(childVisibility) > visibilityRank(parentVisibility)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La visibilite du projet depasse celle du portfolio");
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
