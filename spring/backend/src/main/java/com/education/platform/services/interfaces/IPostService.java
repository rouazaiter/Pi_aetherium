package com.education.platform.services.interfaces;

import com.education.platform.entities.blog.Post;

import java.util.List;

public interface IPostService {

    Post createPost(Post post, Long authorId);

    Post updatePost(Long postId, Post updated, Long requesterId);

    void deletePost(Long postId, Long requesterId);

    Post getPostById(Long postId);

    Post getPostBySlug(String slug);

    List<Post> getPostsByAuthor(Long authorId);

    /** Returns all posts visible to the given viewer */
    List<Post> getVisiblePosts(Long viewerId);

    void addRestrictedUser(Long postId, Long userId, Long requesterId);

    void removeRestrictedUser(Long postId, Long userId, Long requesterId);
}
