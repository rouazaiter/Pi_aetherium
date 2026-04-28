package tn.esprit.aetherium.skillhubbackend.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Like;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);

    long countByPostId(Long postId);

    long countByCommentId(Long commentId);
}
