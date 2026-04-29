package com.education.platform.controllers.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.education.platform.entities.blog.Post;
import com.education.platform.services.interfaces.IPostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final IPostService postService;

    public PostController(IPostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post,
                                           @RequestParam Long authorId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(post, authorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id,
                                           @RequestBody Post post,
                                           @RequestParam Long requesterId) {
        return ResponseEntity.ok(postService.updatePost(id, post, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @RequestParam Long requesterId) {
        postService.deletePost(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Post> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Post>> getByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
    }

    @GetMapping
    public ResponseEntity<List<Post>> getVisiblePosts(@RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(postService.getVisiblePosts(viewerId));
    }

    @PostMapping("/{postId}/restricted-users/{userId}")
    public ResponseEntity<Void> addRestrictedUser(@PathVariable Long postId,
                                                   @PathVariable Long userId,
                                                   @RequestParam Long requesterId) {
        postService.addRestrictedUser(postId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/restricted-users/{userId}")
    public ResponseEntity<Void> removeRestrictedUser(@PathVariable Long postId,
                                                      @PathVariable Long userId,
                                                      @RequestParam Long requesterId) {
        postService.removeRestrictedUser(postId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
