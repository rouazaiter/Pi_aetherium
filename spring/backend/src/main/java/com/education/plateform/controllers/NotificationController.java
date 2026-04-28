package com.education.plateform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.education.plateform.dto.NotificationDto;
import com.education.plateform.services.implementations.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return ResponseEntity.ok(notificationService.getRecentNotifications(userId, limit));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearUserNotifications(@PathVariable Long userId) {
        notificationService.clearNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}