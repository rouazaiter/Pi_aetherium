package skillhub.portfolio_skillhub.repositories;

import skillhub.portfolio_skillhub.entities.Skill;
import skillhub.portfolio_skillhub.enums.SkillCategory;
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

    List<Skill> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Skill s SET s.searchCount = s.searchCount + 1 WHERE s.id = :skillId")
    void incrementSearchCount(@Param("skillId") Long skillId);

    @Modifying
    @Query("UPDATE Skill s SET s.isTrendy = true WHERE s.searchCount > :threshold")
    void markTrendySkills(@Param("threshold") Integer threshold);

    @Modifying
    @Query("UPDATE Skill s SET s.isTrendy = false")
    void unmarkAllTrendySkills();
}