package com.education.platform.controllers.portfolio;

import com.education.platform.dto.portfolio.CreatePortfolioRequest;
import com.education.platform.dto.portfolio.PortfolioResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final CurrentUserService currentUserService;
    private final PortfolioService portfolioService;

    public PortfolioController(CurrentUserService currentUserService, PortfolioService portfolioService) {
        this.currentUserService = currentUserService;
        this.portfolioService = portfolioService;
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse createMyPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        return portfolioService.createForUser(currentUserService.getCurrentUser(), request);
    }

    @GetMapping("/me")
    public PortfolioResponse getMyPortfolio() {
        return portfolioService.getForUser(currentUserService.getCurrentUser());
    }

    @PutMapping("/me")
    public PortfolioResponse updateMyPortfolio(@Valid @RequestBody UpdatePortfolioRequest request) {
        return portfolioService.updateForUser(currentUserService.getCurrentUser(), request);
    }
}
