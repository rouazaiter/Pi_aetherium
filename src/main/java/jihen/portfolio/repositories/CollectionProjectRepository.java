package jihen.portfolio.repositories;

import jihen.portfolio.entities.Collection;
import jihen.portfolio.entities.CollectionProject;
import jihen.portfolio.entities.PortfolioProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionProjectRepository extends JpaRepository<CollectionProject, Long> {

    List<CollectionProject> findByCollection(Collection collection);

    List<CollectionProject> findByCollectionId(Long collectionId);

    List<CollectionProject> findByCollectionIdOrderByOrderIndexAsc(Long collectionId);

    List<CollectionProject> findByProject(PortfolioProject project);

    List<CollectionProject> findByProjectId(Long projectId);

    Optional<CollectionProject> findByCollectionIdAndProjectId(Long collectionId, Long projectId);

    boolean existsByCollectionIdAndProjectId(Long collectionId, Long projectId);

    @Modifying
    @Query("UPDATE CollectionProject cp SET cp.orderIndex = :newOrder WHERE cp.id = :id")
    void updateOrderIndex(@Param("id") Long id, @Param("newOrder") Integer newOrder);

    @Modifying
    void deleteByCollectionId(Long collectionId);

    @Modifying
    void deleteByProjectId(Long projectId);

    @Query("SELECT COALESCE(MAX(cp.orderIndex), 0) + 1 FROM CollectionProject cp WHERE cp.collection.id = :collectionId")
    Integer getNextOrderIndex(@Param("collectionId") Long collectionId);
}