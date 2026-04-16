package jihen.portfolio.repositories;

import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.enums.PortfolioVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    List<Portfolio> findByVisibility(PortfolioVisibility visibility);

    Page<Portfolio> findByVisibility(PortfolioVisibility visibility, Pageable pageable);

    List<Portfolio> findByLocationContainingIgnoreCase(String location);

    List<Portfolio> findByTotalViewsGreaterThanOrderByTotalViewsDesc(Integer minViews);

    @Query("SELECT p FROM Portfolio p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.bio) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Portfolio> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COALESCE(SUM(p.totalViews), 0) FROM Portfolio p")
    Long getTotalViewsSum();

    @Modifying
    @Query("UPDATE Portfolio p SET p.totalViews = p.totalViews + 1 WHERE p.id = :portfolioId")
    void incrementViewCount(@Param("portfolioId") Long portfolioId);

    @Modifying
    @Query("UPDATE Portfolio p SET p.visibility = :visibility WHERE p.id = :portfolioId")
    void updateVisibility(@Param("portfolioId") Long portfolioId,
                          @Param("visibility") PortfolioVisibility visibility);

    @Modifying
    void deleteByUserId(Long userId);
}