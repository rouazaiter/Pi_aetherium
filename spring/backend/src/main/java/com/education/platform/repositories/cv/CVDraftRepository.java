package com.education.platform.repositories.cv;

import com.education.platform.entities.cv.CVDraft;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CVDraftRepository extends JpaRepository<CVDraft, Long> {

    @EntityGraph(attributePaths = "sections")
    Optional<CVDraft> findTopByUser_IdOrderByUpdatedAtDescIdDesc(Long userId);

    @EntityGraph(attributePaths = "sections")
    Optional<CVDraft> findByIdAndUser_Id(Long id, Long userId);
}
