package tn.esprit.aetherium.skillhubbackend.controllers.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.aetherium.skillhubbackend.entities.blog.KnowledgeBaseArticle;
import tn.esprit.aetherium.skillhubbackend.services.implementations.KnowledgeBaseServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {

    private final KnowledgeBaseServiceImpl kbService;

    public KnowledgeBaseController(KnowledgeBaseServiceImpl kbService) {
        this.kbService = kbService;
    }

    /** Mark a discussion as solved and auto-create KB article */
    @PostMapping("/solve")
    public ResponseEntity<KnowledgeBaseArticle> markSolved(
            @RequestParam Long discussionId,
            @RequestParam Long acceptedMessageId,
            @RequestParam Long requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kbService.markSolved(discussionId, acceptedMessageId, requesterId));
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeBaseArticle>> getAll() {
        return ResponseEntity.ok(kbService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeBaseArticle>> search(@RequestParam String q) {
        return ResponseEntity.ok(kbService.search(q));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<KnowledgeBaseArticle>> popular() {
        return ResponseEntity.ok(kbService.getPopular());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeBaseArticle> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kbService.getById(id));
    }
}
