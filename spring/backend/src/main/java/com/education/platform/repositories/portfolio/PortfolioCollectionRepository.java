package com.education.platform.repositories.portfolio;

import com.education.platform.entities.portfolio.PortfolioCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioCollectionRepository extends JpaRepository<PortfolioCollection, Long> {

    List<PortfolioCollection> findByPortfolio_IdOrderByCreatedAtDesc(Long portfolioId);

    Optional<PortfolioCollection> findByIdAndPortfolio_User_Id(Long collectionId, Long userId);
}
