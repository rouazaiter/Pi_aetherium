package com.education.platform.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.education.platform.entities.blog.Discussion;

import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    List<Discussion> findByCreatorId(Long creatorId);

    @Query("SELECT d FROM Discussion d JOIN d.participants p WHERE p.id = :userId AND d.status <> com.education.platform.entities.blog.DiscussionStatus.DELETED")
    List<Discussion> findByParticipantId(@Param("userId") Long userId);
}
