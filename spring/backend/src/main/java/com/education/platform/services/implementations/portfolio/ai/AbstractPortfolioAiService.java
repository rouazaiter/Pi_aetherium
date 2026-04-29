package com.education.platform.services.implementations.portfolio.ai;

import com.education.platform.common.ApiException;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import org.springframework.http.HttpStatus;

abstract class AbstractPortfolioAiService {

    private final CurrentUserService currentUserService;
    private final PortfolioRepository portfolioRepository;

    protected AbstractPortfolioAiService(CurrentUserService currentUserService, PortfolioRepository portfolioRepository) {
        this.currentUserService = currentUserService;
        this.portfolioRepository = portfolioRepository;
    }

    protected Portfolio getCurrentUserPortfolio() {
        return portfolioRepository.findByUser_Id(currentUserService.getCurrentUser().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }
}
