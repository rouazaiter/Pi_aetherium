package jihen.portfolio.repositories;

import jihen.portfolio.entities.ScenarioSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

    public interface ScenarioSkillRepository extends JpaRepository<ScenarioSkill, Long> {

        Set<ScenarioSkill> findByScenarioId(Long scenarioId);
    }

