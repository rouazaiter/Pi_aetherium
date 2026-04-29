package com.education.platform.services.interfaces.portfolio;

import com.education.platform.dto.portfolio.CreatePortfolioCollectionRequest;
import com.education.platform.dto.portfolio.PortfolioCollectionResponse;
import com.education.platform.dto.portfolio.UpdatePortfolioCollectionRequest;
import com.education.platform.entities.User;

import java.util.List;

public interface PortfolioCollectionService {

    List<PortfolioCollectionResponse> listForUser(User user);

    PortfolioCollectionResponse createForUser(User user, CreatePortfolioCollectionRequest request);

    PortfolioCollectionResponse updateForUser(User user, Long collectionId, UpdatePortfolioCollectionRequest request);

    void deleteForUser(User user, Long collectionId);

    PortfolioCollectionResponse addProjectToCollection(User user, Long collectionId, Long projectId);

    PortfolioCollectionResponse removeProjectFromCollection(User user, Long collectionId, Long projectId);
}
