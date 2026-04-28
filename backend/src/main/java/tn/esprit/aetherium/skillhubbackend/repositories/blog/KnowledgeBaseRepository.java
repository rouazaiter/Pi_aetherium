package tn.esprit.aetherium.skillhubbackend.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.aetherium.skillhubbackend.entities.blog.KnowledgeBaseArticle;

import java.util.List;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseArticle, Long> {

    @Query("SELECT k FROM KnowledgeBaseArticle k WHERE " +
           "k.question LIKE CONCAT('%', :q, '%') OR " +
           "k.answer LIKE CONCAT('%', :q, '%') OR " +
           "k.tags LIKE CONCAT('%', :q, '%')")
    List<KnowledgeBaseArticle> search(@Param("q") String query);

    List<KnowledgeBaseArticle> findByTagsContainingIgnoreCase(String tag);

    List<KnowledgeBaseArticle> findAllByOrderByViewsDesc();
}
