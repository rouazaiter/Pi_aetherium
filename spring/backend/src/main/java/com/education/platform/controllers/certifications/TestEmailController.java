package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.services.implementations.certifications.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-email")
@RequiredArgsConstructor
public class TestEmailController {

    private final EmailService emailService;

    @GetMapping
    public String testEmail(@RequestParam String to) {
        try {
            emailService.sendVerificationCode(to, "123456", "Test Certification");
            return "Test email sent successfully to " + to + ". Check your inbox!";
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}
