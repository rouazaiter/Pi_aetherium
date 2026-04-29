package com.education.platform.controllers.blog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.education.platform.services.interfaces.ILikeService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LikeController {

    private final ILikeService likeService;

    public LikeController(ILikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        likeService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        likeService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/likes/count")
    public ResponseEntity<Map<String, Long>> countPostLikes(@PathVariable Long postId) {
        return ResponseEntity.ok(Map.of("count", likeService.countPostLikes(postId)));
    }

    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        likeService.likeComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}/likes")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        likeService.unlikeComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments/{commentId}/likes/count")
    public ResponseEntity<Map<String, Long>> countCommentLikes(@PathVariable Long commentId) {
        return ResponseEntity.ok(Map.of("count", likeService.countCommentLikes(commentId)));
    }
}
