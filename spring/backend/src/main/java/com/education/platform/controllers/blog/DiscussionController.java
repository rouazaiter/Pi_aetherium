package com.education.platform.controllers.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.education.platform.entities.blog.Discussion;
import com.education.platform.services.interfaces.IDiscussionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private final IDiscussionService discussionService;

    public DiscussionController(IDiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @PostMapping
    public ResponseEntity<Discussion> create(@RequestParam Long creatorId,
                                             @RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(discussionService.createDiscussion(body.get("theme"), creatorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Discussion> update(@PathVariable Long id,
                                             @RequestParam Long requesterId,
                                             @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(discussionService.updateDiscussion(id, body.get("theme"), requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long requesterId) {
        discussionService.deleteDiscussion(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Discussion> getById(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.getDiscussionById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Discussion>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(discussionService.getDiscussionsByUser(userId));
    }

    @PostMapping("/{id}/participants/{userId}")
    public ResponseEntity<Void> addParticipant(@PathVariable Long id,
                                               @PathVariable Long userId,
                                               @RequestParam Long requesterId) {
        discussionService.addParticipant(id, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable Long id,
                                                   @PathVariable Long userId,
                                                   @RequestParam Long requesterId) {
        discussionService.removeParticipant(id, userId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
