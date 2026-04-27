package com.education.platform.services.implementations.portfolio;

import com.education.platform.common.ApiException;
import com.education.platform.dto.portfolio.CreatePortfolioRequest;
import com.education.platform.dto.portfolio.PortfolioResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioCollection;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.portfolio.PortfolioCollectionRepository;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.portfolio.PortfolioService;
import com.education.platform.services.interfaces.portfolio.SkillCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final PortfolioCollectionRepository portfolioCollectionRepository;
    private final SkillCatalogService skillCatalogService;
    private final PortfolioMapper portfolioMapper;

    public PortfolioServiceImpl(
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            PortfolioCollectionRepository portfolioCollectionRepository,
            SkillCatalogService skillCatalogService,
            PortfolioMapper portfolioMapper) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.portfolioCollectionRepository = portfolioCollectionRepository;
        this.skillCatalogService = skillCatalogService;
        this.portfolioMapper = portfolioMapper;
    }

    @Override
    @Transactional
    public PortfolioResponse createForUser(User user, CreatePortfolioRequest request) {
        if (portfolioRepository.existsByUser_Id(user.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Vous avez deja un portfolio");
        }

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .title(request.getTitle())
                .bio(request.getBio())
                .coverImage(request.getCoverImage())
                .job(request.getJob())
                .githubUrl(request.getGithubUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .openToWork(Boolean.TRUE.equals(request.getOpenToWork()))
                .availableForFreelance(Boolean.TRUE.equals(request.getAvailableForFreelance()))
                .visibility(request.getVisibility() == null ? Visibility.PRIVATE : request.getVisibility())
                .skills(new LinkedHashSet<>())
                .build();

        portfolio.getSkills().addAll(skillCatalogService.requireSkillsByIds(request.getSkillIds()));
        Portfolio saved = portfolioRepository.save(portfolio);
        return buildOwnerResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getForUser(User user) {
        return buildOwnerResponse(requirePortfolio(user));
    }

    @Override
    @Transactional
    public PortfolioResponse updateForUser(User user, UpdatePortfolioRequest request) {
        Portfolio portfolio = requirePortfolio(user);
        Visibility previousVisibility = portfolio.getVisibility();

        if (request.getTitle() != null) {
            portfolio.setTitle(request.getTitle());
        }
        if (request.getBio() != null) {
            portfolio.setBio(request.getBio());
        }
        if (request.getCoverImage() != null) {
            portfolio.setCoverImage(request.getCoverImage());
        }
        if (request.getJob() != null) {
            portfolio.setJob(request.getJob());
        }
        if (request.getGithubUrl() != null) {
            portfolio.setGithubUrl(request.getGithubUrl());
        }
        if (request.getLinkedinUrl() != null) {
            portfolio.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getOpenToWork() != null) {
            portfolio.setOpenToWork(request.getOpenToWork());
        }
        if (request.getAvailableForFreelance() != null) {
            portfolio.setAvailableForFreelance(request.getAvailableForFreelance());
        }
        if (request.getVisibility() != null) {
            portfolio.setVisibility(request.getVisibility());
            if (isMoreRestrictive(previousVisibility, request.getVisibility())) {
                rewriteChildVisibilities(portfolio, request.getVisibility());
            }
        }
        if (request.getSkillIds() != null) {
            portfolio.getSkills().clear();
            portfolio.getSkills().addAll(skillCatalogService.requireSkillsByIds(request.getSkillIds()));
        }

        return buildOwnerResponse(portfolio);
    }

    private Portfolio requirePortfolio(User user) {
        return portfolioRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Portfolio introuvable"));
    }

    private PortfolioResponse buildOwnerResponse(Portfolio portfolio) {
        List<PortfolioProject> projects = portfolioProjectRepository.findByPortfolio_IdOrderByCreatedAtDesc(portfolio.getId());
        List<PortfolioCollection> collections = portfolioCollectionRepository.findByPortfolio_IdOrderByCreatedAtDesc(portfolio.getId());
        return portfolioMapper.toPortfolioResponse(portfolio, projects, collections, true);
    }

    private void rewriteChildVisibilities(Portfolio portfolio, Visibility newVisibility) {
        for (PortfolioProject project : portfolio.getProjects()) {
            project.setVisibility(clampToParent(newVisibility, project.getVisibility()));
        }
        for (PortfolioCollection collection : portfolio.getCollections()) {
            collection.setVisibility(clampToParent(newVisibility, collection.getVisibility()));
        }
    }

    private Visibility clampToParent(Visibility parent, Visibility child) {
        if (child == null || visibilityRank(child) > visibilityRank(parent)) {
            return parent;
        }
        return child;
    }

    private boolean isMoreRestrictive(Visibility previous, Visibility next) {
        return visibilityRank(next) < visibilityRank(previous);
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
