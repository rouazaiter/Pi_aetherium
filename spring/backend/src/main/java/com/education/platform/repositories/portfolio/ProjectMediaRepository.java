package com.education.platform.repositories.portfolio;

import com.education.platform.entities.portfolio.ProjectMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMediaRepository extends JpaRepository<ProjectMedia, Long> {

    List<ProjectMedia> findByProject_IdOrderByOrderIndexAscIdAsc(Long projectId);

    void deleteByProject_Id(Long projectId);
}
