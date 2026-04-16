package jihen.portfolio.repositories;


import jihen.portfolio.entities.PortfolioTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

    @Repository
    public interface PortfolioThemeRepository extends JpaRepository<PortfolioTheme, Long> {

            List<PortfolioTheme> findByPortfolioId(Long portfolioId);

            Optional<PortfolioTheme> findByPortfolioIdAndIsActiveTrue(Long portfolioId);

            boolean existsByPortfolioIdAndName(Long portfolioId, String name);
        }

