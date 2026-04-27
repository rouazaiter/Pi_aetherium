package com.education.platform.repositories.portfolio;

import com.education.platform.entities.portfolio.PortfolioProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PortfolioProjectRepository extends JpaRepository<PortfolioProject, Long> {

    List<PortfolioProject> findByPortfolio_IdOrderByCreatedAtDesc(Long portfolioId);

    Optional<PortfolioProject> findByIdAndPortfolio_User_Id(Long projectId, Long userId);

    @Query(
            """
            select distinct pr
            from PortfolioProject pr
            join pr.skills s
            where s.id = :skillId
            """
    )
    List<PortfolioProject> findDistinctBySkills_Id(@Param("skillId") Long skillId);
}
