package com.education.platform.services.interfaces;

public interface ILikeService {

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    void likeComment(Long commentId, Long userId);

    void unlikeComment(Long commentId, Long userId);

    long countPostLikes(Long postId);

    long countCommentLikes(Long commentId);
}
