package com.education.platform.controllers.blog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.education.platform.entities.blog.Reaction;
import com.education.platform.entities.blog.ReactionType;
import com.education.platform.services.interfaces.IReactionService;

import java.util.List;

@RestController
@RequestMapping("/api/messages/{messageId}/reactions")
public class ReactionController {

    private final IReactionService reactionService;

    public ReactionController(IReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping
    public ResponseEntity<Reaction> react(@PathVariable Long messageId,
                                          @RequestParam Long userId,
                                          @RequestParam ReactionType type) {
        return ResponseEntity.ok(reactionService.reactToMessage(messageId, userId, type));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeReaction(@PathVariable Long messageId,
                                               @RequestParam Long userId) {
        reactionService.removeReaction(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Reaction>> getReactions(@PathVariable Long messageId) {
        return ResponseEntity.ok(reactionService.getReactionsByMessage(messageId));
    }
}
