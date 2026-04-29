package com.education.platform.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.education.platform.entities.blog.Post;
import com.education.platform.entities.blog.PostVisibility;
import com.education.platform.entities.user.User;
import com.education.platform.repositories.blog.PostRepository;
import com.education.platform.repositories.user.UserRepository;
import com.education.platform.services.interfaces.IPostService;

import java.util.ArrayList;
import java.util.List;

import com.education.platform.config.ModerationException;
import com.education.platform.services.implementations.ContentModerationServiceImpl;
import com.education.platform.services.implementations.UrgencyDetectionServiceImpl;

@Service
@Transactional
public class PostServiceImpl implements IPostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ContentModerationServiceImpl moderation;
    private final UrgencyDetectionServiceImpl urgency;

    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           ContentModerationServiceImpl moderation,
                           UrgencyDetectionServiceImpl urgency) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.moderation = moderation;
        this.urgency = urgency;
    }

    @Override
    public Post createPost(Post post, Long authorId) {
        checkContent(post.getTitle(), post.getContent());
        post.setUrgencyLevel(urgency.detect(post.getTitle(), post.getContent()).level().name());
        User author = findUser(authorId);
        post.setAuthor(author);
        return postRepository.save(post);
    }

    @Override
    public Post updatePost(Long postId, Post updated, Long requesterId) {
        checkContent(updated.getTitle(), updated.getContent());
        Post post = findPost(postId);
        assertOwner(post.getAuthor().getId(), requesterId);
        post.setTitle(updated.getTitle());
        post.setSlug(updated.getSlug());
        post.setContent(updated.getContent());
        post.setCoverImage(updated.getCoverImage());
        post.setStatus(updated.getStatus());
        post.setVisibility(updated.getVisibility());
        post.setUrgencyLevel(urgency.detect(updated.getTitle(), updated.getContent()).level().name());
        return postRepository.save(post);
    }

    @Override
    public void deletePost(Long postId, Long requesterId) {
        Post post = findPost(postId);
        assertOwner(post.getAuthor().getId(), requesterId);
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPostById(Long postId) {
        return findPost(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getVisiblePosts(Long viewerId) {
        List<Post> visible = new ArrayList<>(postRepository.findAllPublicPublished());
        if (viewerId != null) {
            visible.addAll(postRepository.findFriendsOnlyPostsVisibleTo(viewerId));
            visible.addAll(postRepository.findRestrictedPostsVisibleTo(viewerId));
            // Add the viewer's own posts
            postRepository.findByAuthorId(viewerId).stream()
                    .filter(p -> !visible.contains(p))
                    .forEach(visible::add);
        }
        return visible;
    }

    @Override
    public void addRestrictedUser(Long postId, Long userId, Long requesterId) {
        Post post = findPost(postId);
        assertOwner(post.getAuthor().getId(), requesterId);
        if (post.getVisibility() != PostVisibility.RESTRICTED) {
            throw new RuntimeException("Post visibility must be RESTRICTED to manage restricted users");
        }
        User user = findUser(userId);
        post.getRestrictedUsers().add(user);
        postRepository.save(post);
    }

    @Override
    public void removeRestrictedUser(Long postId, Long userId, Long requesterId) {
        Post post = findPost(postId);
        assertOwner(post.getAuthor().getId(), requesterId);
        post.getRestrictedUsers().removeIf(u -> u.getId().equals(userId));
        postRepository.save(post);
    }

    // --- helpers ---

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private void assertOwner(Long ownerId, Long requesterId) {
        if (!ownerId.equals(requesterId)) {
            throw new RuntimeException("Access denied: requester is not the owner");
        }
    }

    private void checkContent(String... fields) {
        for (String field : fields) {
            if (field == null) continue;
            ContentModerationServiceImpl.ModerationResult result = moderation.check(field);
            if (result.flagged()) throw new ModerationException(result.detectedWords());
        }
    }
}
