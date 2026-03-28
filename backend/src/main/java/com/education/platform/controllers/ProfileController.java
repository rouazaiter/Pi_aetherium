package com.education.platform.controllers;

import com.education.platform.dto.profile.ProfileResponse;
import com.education.platform.dto.profile.ProfileUpdateRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CurrentUserService currentUserService;
    private final ProfileService profileService;

    public ProfileController(CurrentUserService currentUserService, ProfileService profileService) {
        this.currentUserService = currentUserService;
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ProfileResponse me() {
        return profileService.getForUser(currentUserService.getCurrentUser());
    }

    @PutMapping("/me")
    public ProfileResponse updateMe(@Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.updateForUser(currentUserService.getCurrentUser(), request);
    }
}
