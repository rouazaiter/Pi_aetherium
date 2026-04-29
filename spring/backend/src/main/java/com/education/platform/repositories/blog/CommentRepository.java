package com.education.platform.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import com.education.platform.entities.blog.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostId(Long postId);

    List<Comment> findByAuthorId(Long authorId);
}
