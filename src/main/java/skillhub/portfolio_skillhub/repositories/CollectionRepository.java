package skillhub.portfolio_skillhub.repositories;

import skillhub.portfolio_skillhub.entities.Collection;
import skillhub.portfolio_skillhub.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findByPortfolio(Portfolio portfolio);

    List<Collection> findByPortfolioId(Long portfolioId);

    List<Collection> findByNameContainingIgnoreCase(String name);

    List<Collection> findByPortfolioIdAndNameContainingIgnoreCase(Long portfolioId, String name);

    boolean existsByPortfolioIdAndName(Long portfolioId, String name);

    @Modifying
    void deleteByPortfolioId(Long portfolioId);
}