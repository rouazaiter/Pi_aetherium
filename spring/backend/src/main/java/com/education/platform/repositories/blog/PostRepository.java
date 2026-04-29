package com.education.platform.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.education.platform.entities.blog.Post;
import com.education.platform.entities.blog.PostStatus;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);

    List<Post> findByAuthorId(Long authorId);

    List<Post> findByStatus(PostStatus status);

    // Posts visible to a given viewer: PUBLIC published posts
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.visibility = 'PUBLIC'")
    List<Post> findAllPublicPublished();

    // Posts visible to friends
    @Query("""
        SELECT p FROM Post p
        WHERE p.status = 'PUBLISHED'
          AND p.visibility = 'FRIENDS_ONLY'
          AND EXISTS (
              SELECT 1 FROM User u JOIN u.friends f
              WHERE u.id = p.author.id AND f.id = :viewerId
          )
    """)
    List<Post> findFriendsOnlyPostsVisibleTo(@Param("viewerId") Long viewerId);

    // RESTRICTED posts where viewer is in the allowed list
    @Query("""
        SELECT p FROM Post p
        JOIN p.restrictedUsers ru
        WHERE p.status = 'PUBLISHED'
          AND p.visibility = 'RESTRICTED'
          AND ru.id = :viewerId
    """)
    List<Post> findRestrictedPostsVisibleTo(@Param("viewerId") Long viewerId);
}
