package tn.esprit.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.backend.dto.UserSummaryResponse;
import tn.esprit.backend.repositories.UserRepository;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserSummaryResponse> getUsers(@RequestParam(required = false) Integer limit) {
        var stream = userRepository.findAll().stream()
                .sorted(Comparator.comparingLong(user -> user.getId() == null ? Long.MAX_VALUE : user.getId()))
                .map(user -> new UserSummaryResponse(user.getId(), user.getUsername(), user.getEmail()));

        if (limit != null) {
            int safeLimit = Math.max(1, Math.min(limit, 50));
            return stream.limit(safeLimit).toList();
        }

        return stream.toList();
    }
}