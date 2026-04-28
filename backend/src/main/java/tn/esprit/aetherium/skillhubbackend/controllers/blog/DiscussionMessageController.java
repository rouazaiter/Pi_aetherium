package tn.esprit.aetherium.skillhubbackend.controllers.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.aetherium.skillhubbackend.entities.blog.DiscussionMessage;
import tn.esprit.aetherium.skillhubbackend.services.interfaces.IDiscussionMessageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions/{discussionId}/messages")
public class DiscussionMessageController {

    private final IDiscussionMessageService messageService;

    public DiscussionMessageController(IDiscussionMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<DiscussionMessage> send(@PathVariable Long discussionId,
                                                   @RequestParam Long senderId,
                                                   @RequestParam(required = false) Long parentId,
                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(discussionId, senderId, body.get("content"), parentId));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<DiscussionMessage> update(@PathVariable Long discussionId,
                                                     @PathVariable Long messageId,
                                                     @RequestParam Long requesterId,
                                                     @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(messageService.updateMessage(messageId, body.get("content"), requesterId));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable Long discussionId,
                                       @PathVariable Long messageId,
                                       @RequestParam Long requesterId) {
        messageService.deleteMessage(messageId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DiscussionMessage>> getTopLevel(@PathVariable Long discussionId) {
        return ResponseEntity.ok(messageService.getTopLevelMessages(discussionId));
    }

    @GetMapping("/{messageId}/replies")
    public ResponseEntity<List<DiscussionMessage>> getReplies(@PathVariable Long discussionId,
                                                               @PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getReplies(messageId));
    }
}
