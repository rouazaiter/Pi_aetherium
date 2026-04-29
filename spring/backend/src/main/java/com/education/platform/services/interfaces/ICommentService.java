package com.education.platform.services.interfaces;

import com.education.platform.entities.blog.Comment;

import java.util.List;

public interface ICommentService {

    Comment addComment(Long postId, Long authorId, String content);

    Comment updateComment(Long commentId, String content, Long requesterId);

    void deleteComment(Long commentId, Long requesterId);

    List<Comment> getCommentsByPost(Long postId);
}
