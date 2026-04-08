package tn.esprit.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.backend.dto.LeaderboardResponseDto;
import tn.esprit.backend.services.interfaces.LeaderboardService;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/applicants")
    public ResponseEntity<LeaderboardResponseDto> getApplicantsLeaderboard(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(leaderboardService.getApplicantsLeaderboard(days, limit));
    }

    @GetMapping("/creators")
    public ResponseEntity<LeaderboardResponseDto> getCreatorsLeaderboard(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(leaderboardService.getCreatorsLeaderboard(days, limit));
    }
}