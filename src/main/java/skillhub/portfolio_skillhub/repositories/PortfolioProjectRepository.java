package skillhub.portfolio_skillhub.repositories;

import skillhub.portfolio_skillhub.entities.Portfolio;
import skillhub.portfolio_skillhub.entities.PortfolioProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioProjectRepository extends JpaRepository<PortfolioProject, Long> {

    List<PortfolioProject> findByPortfolio(Portfolio portfolio);

    List<PortfolioProject> findByPortfolioId(Long portfolioId);

    List<PortfolioProject> findByPortfolioIdAndIsPinnedTrue(Long portfolioId);

    List<PortfolioProject> findByPortfolioIdOrderByViewsDesc(Long portfolioId);

    List<PortfolioProject> findByPortfolioIdOrderByLikesDesc(Long portfolioId);

    List<PortfolioProject> findByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);

    @Modifying
    @Query("UPDATE PortfolioProject p SET p.views = p.views + 1 WHERE p.id = :projectId")
    void incrementViews(@Param("projectId") Long projectId);

    @Modifying
    @Query("UPDATE PortfolioProject p SET p.likes = p.likes + 1 WHERE p.id = :projectId")
    void incrementLikes(@Param("projectId") Long projectId);

    @Modifying
    @Query("UPDATE PortfolioProject p SET p.isPinned = :isPinned WHERE p.id = :projectId")
    void updatePinnedStatus(@Param("projectId") Long projectId, @Param("isPinned") Boolean isPinned);
}