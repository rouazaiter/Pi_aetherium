package jihen.portfolio.repositories;

import jakarta.transaction.Transactional;
import jihen.portfolio.entities.Portfolio;
import jihen.portfolio.entities.Skill;
import jihen.portfolio.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByName(String name);

    boolean existsByName(String name);

    List<Skill> findByTypecategory(SkillCategory typecategory);
    List<Skill> findByIsTrendyTrue();

    List<Skill> findAllByOrderBySearchCountDesc();

    List<Skill> findTop10ByOrderBySearchCountDesc();

    List<Skill> findByNameContainingIgnoreCase(String name);
    @Modifying
    @Query("SELECT DISTINCT p FROM Portfolio p JOIN p.skills s WHERE s.id = :skillId")
    List<Portfolio> findPortfoliosBySkillId(@Param("skillId") Long skillId);

    @Modifying
    @Transactional
    @Query("UPDATE Skill s SET s.searchCount = s.searchCount + 1 WHERE s.id = :skillId")
    void incrementSearchCount(@Param("skillId") Long skillId);

    @Modifying
    @Transactional
    @Query("UPDATE Skill s SET s.isTrendy = true WHERE s.searchCount > :threshold")
    void markTrendySkills(@Param("threshold") Integer threshold);

    @Modifying
    @Transactional
    @Query("UPDATE Skill s SET s.isTrendy = false")
    void unmarkAllTrendySkills();
}