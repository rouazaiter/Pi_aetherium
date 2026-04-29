package com.education.platform.services.interfaces.portfolio;

import com.education.platform.dto.portfolio.CreatePortfolioProjectRequest;
import com.education.platform.dto.portfolio.PortfolioProjectResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioProjectRequest;
import com.education.platform.entities.User;

import java.util.List;

public interface PortfolioProjectService {

    List<PortfolioProjectResponse> listForUser(User user);

    PortfolioProjectResponse createForUser(User user, CreatePortfolioProjectRequest request);

    PortfolioProjectResponse updateForUser(User user, Long projectId, UpdatePortfolioProjectRequest request);

    void deleteForUser(User user, Long projectId);
}
