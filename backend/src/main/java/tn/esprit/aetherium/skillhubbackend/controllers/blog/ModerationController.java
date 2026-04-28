package tn.esprit.aetherium.skillhubbackend.controllers.blog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.aetherium.skillhubbackend.services.implementations.ModerationViolationServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
public class ModerationController {

    private final ModerationViolationServiceImpl violationService;

    public ModerationController(ModerationViolationServiceImpl violationService) {
        this.violationService = violationService;
    }

    @GetMapping("/violations/{userId}")
    public ResponseEntity<Map<String, Integer>> getViolations(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("count", violationService.getViolationCount(userId)));
    }
}
