package com.education.platform.services.interfaces.portfolio;

import com.education.platform.dto.portfolio.CreatePortfolioRequest;
import com.education.platform.dto.portfolio.PortfolioResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioRequest;
import com.education.platform.entities.User;

public interface PortfolioService {

    PortfolioResponse createForUser(User user, CreatePortfolioRequest request);

    PortfolioResponse getForUser(User user);

    PortfolioResponse updateForUser(User user, UpdatePortfolioRequest request);
}
