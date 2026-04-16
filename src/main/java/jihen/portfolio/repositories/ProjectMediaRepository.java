package jihen.portfolio.repositories;


import jihen.portfolio.entities.PortfolioProject;
import jihen.portfolio.entities.ProjectMedia;
import jihen.portfolio.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMediaRepository extends JpaRepository<ProjectMedia, Long> {

    List<ProjectMedia> findByProject(PortfolioProject project);

    List<ProjectMedia> findByProjectId(Long projectId);

    List<ProjectMedia> findByProjectIdAndMediatype(Long projectId, MediaType mediatype);

    List<ProjectMedia> findByProjectIdOrderByOrderIndexAsc(Long projectId);

    void deleteByProjectId(Long projectId);
}
