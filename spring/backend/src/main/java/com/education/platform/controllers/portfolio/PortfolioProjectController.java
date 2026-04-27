package com.education.platform.controllers.portfolio;

import com.education.platform.dto.portfolio.CreatePortfolioProjectRequest;
import com.education.platform.dto.portfolio.PortfolioProjectResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioProjectRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.portfolio.PortfolioProjectService;
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
@RequestMapping("/api/portfolio/me/projects")
public class PortfolioProjectController {

    private final CurrentUserService currentUserService;
    private final PortfolioProjectService portfolioProjectService;

    public PortfolioProjectController(CurrentUserService currentUserService, PortfolioProjectService portfolioProjectService) {
        this.currentUserService = currentUserService;
        this.portfolioProjectService = portfolioProjectService;
    }

    @GetMapping
    public List<PortfolioProjectResponse> myProjects() {
        return portfolioProjectService.listForUser(currentUserService.getCurrentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioProjectResponse createProject(@Valid @RequestBody CreatePortfolioProjectRequest request) {
        return portfolioProjectService.createForUser(currentUserService.getCurrentUser(), request);
    }

    @PutMapping("/{projectId}")
    public PortfolioProjectResponse updateProject(@PathVariable Long projectId, @Valid @RequestBody UpdatePortfolioProjectRequest request) {
        return portfolioProjectService.updateForUser(currentUserService.getCurrentUser(), projectId, request);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long projectId) {
        portfolioProjectService.deleteForUser(currentUserService.getCurrentUser(), projectId);
    }
}
