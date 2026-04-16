package jihen.portfolio.repositories;


import jihen.portfolio.entities.PortfolioScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioScenarioRepository extends JpaRepository<PortfolioScenario, Long> {

    Optional<PortfolioScenario> findByScenarioKeyAndActiveTrue(String scenarioKey);

    List<PortfolioScenario> findByActiveTrue();
}

