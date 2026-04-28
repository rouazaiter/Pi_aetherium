package tn.esprit.aetherium.skillhubbackend.controllers.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Comment;
import tn.esprit.aetherium.skillhubbackend.services.interfaces.ICommentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId,
                                              @RequestParam Long authorId,
                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(postId, authorId, body.get("content")));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id,
                                                 @RequestParam Long requesterId,
                                                 @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(commentService.updateComment(id, body.get("content"), requesterId));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id,
                                              @RequestParam Long requesterId) {
        commentService.deleteComment(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}
