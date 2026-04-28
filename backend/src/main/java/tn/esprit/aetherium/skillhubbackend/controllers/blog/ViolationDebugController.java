package tn.esprit.aetherium.skillhubbackend.controllers.blog;

import org.springframework.web.bind.annotation.*;
import tn.esprit.aetherium.skillhubbackend.services.implementations.ModerationViolationServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/debug/violations")
public class ViolationDebugController {

    private final ModerationViolationServiceImpl violationService;

    public ViolationDebugController(ModerationViolationServiceImpl violationService) {
        this.violationService = violationService;
    }

    @DeleteMapping("/reset/{userId}")
    public Map<String, String> reset(@PathVariable Long userId) {
        violationService.resetUser(userId);
        return Map.of("status", "reset", "userId", userId.toString());
    }

    @GetMapping("/count/{userId}")
    public Map<String, String> count(@PathVariable Long userId) {
        return Map.of("userId", userId.toString(), "count", String.valueOf(violationService.getViolationCount(userId)));
    }
}
