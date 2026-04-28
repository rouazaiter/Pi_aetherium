package tn.esprit.aetherium.skillhubbackend.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Discussion;

import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    List<Discussion> findByCreatorId(Long creatorId);

    @Query("SELECT d FROM Discussion d JOIN d.participants p WHERE p.id = :userId AND d.status <> tn.esprit.aetherium.skillhubbackend.entities.blog.DiscussionStatus.DELETED")
    List<Discussion> findByParticipantId(@Param("userId") Long userId);
}
