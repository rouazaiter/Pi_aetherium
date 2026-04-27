package com.education.platform.repositories.portfolio;

import com.education.platform.entities.portfolio.CollectionProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionProjectRepository extends JpaRepository<CollectionProject, Long> {

    List<CollectionProject> findByPortfolioCollection_IdOrderByAddedDateDesc(Long portfolioCollectionId);

    List<CollectionProject> findByProject_IdOrderByAddedDateDesc(Long projectId);

    Optional<CollectionProject> findByPortfolioCollection_IdAndProject_Id(Long portfolioCollectionId, Long projectId);

    boolean existsByPortfolioCollection_IdAndProject_Id(Long portfolioCollectionId, Long projectId);
}
