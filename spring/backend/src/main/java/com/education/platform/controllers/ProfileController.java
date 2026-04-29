package com.education.platform.controllers;

import com.education.platform.dto.profile.LoginActivityResponse;
import com.education.platform.dto.profile.ProfileResponse;
import com.education.platform.dto.profile.TwoFactorUpdateRequest;
import com.education.platform.dto.profile.ActiveStatusVisibilityUpdateRequest;
import com.education.platform.dto.profile.ProfileUpdateRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadPhoto(@RequestPart("file") MultipartFile file) {
        return profileService.updateProfilePhoto(currentUserService.getCurrentUser(), file);
    }

    @PatchMapping("/me/security/two-factor")
    public ProfileResponse updateTwoFactor(@RequestBody TwoFactorUpdateRequest request) {
        return profileService.updateTwoFactorForUser(currentUserService.getCurrentUser(), request.isEnabled());
    }

    @GetMapping("/me/security/login-activity")
    public List<LoginActivityResponse> loginActivity() {
        return profileService.getLoginActivityForUser(currentUserService.getCurrentUser());
    }

    @PatchMapping("/me/privacy/active-status")
    public ProfileResponse updateActiveStatusVisibility(@RequestBody ActiveStatusVisibilityUpdateRequest request) {
        return profileService.updateActiveStatusVisibilityForUser(currentUserService.getCurrentUser(), request.isEnabled());
    }
}
