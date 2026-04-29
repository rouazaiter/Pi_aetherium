package com.education.platform.repositories.portfolio;

import com.education.platform.entities.portfolio.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    Optional<Skill> findByNormalizedName(String normalizedName);

    boolean existsByNameIgnoreCase(String name);

    List<Skill> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    @Query(
            """
            select s
            from Skill s
            where lower(coalesce(s.name, '')) like lower(concat(:query, '%'))
               or lower(coalesce(s.name, '')) like lower(concat('%', :query, '%'))
            order by lower(coalesce(s.name, '')) asc
            """
    )
    List<Skill> searchByNamePrefixOrContains(@Param("query") String query);

    List<Skill> findByIdIn(Collection<Long> ids);
}
