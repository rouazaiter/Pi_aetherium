package com.education.platform.repositories.portfolio;

import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    List<Portfolio> findAllByVisibility(Visibility visibility);

    Page<Portfolio> findAllByVisibility(Visibility visibility, Pageable pageable);

    @Query(
            """
            select p
            from Portfolio p
            where lower(coalesce(p.title, '')) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(p.bio, '')) like lower(concat('%', :keyword, '%'))
            """
    )
    List<Portfolio> searchByKeyword(@Param("keyword") String keyword);

    List<Portfolio> findByTotalViewsGreaterThanOrderByTotalViewsDesc(Long minViews);

    @Query("select coalesce(sum(p.totalViews), 0) from Portfolio p")
    Long getTotalViewsSum();

    @Modifying(clearAutomatically = true)
    @Query("update Portfolio p set p.totalViews = p.totalViews + 1 where p.id = :portfolioId")
    void incrementViewCount(@Param("portfolioId") Long portfolioId);

    @Modifying(clearAutomatically = true)
    @Query("update Portfolio p set p.visibility = :visibility where p.id = :portfolioId")
    void updateVisibility(@Param("portfolioId") Long portfolioId, @Param("visibility") Visibility visibility);

    @Query(
            """
            select distinct p
            from Portfolio p
            join p.skills s
            where s.id = :skillId
            """
    )
    List<Portfolio> findDistinctBySkills_Id(@Param("skillId") Long skillId);
}
