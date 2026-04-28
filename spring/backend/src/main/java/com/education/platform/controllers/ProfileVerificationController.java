package com.education.platform.controllers;

import com.education.platform.dto.auth.MessageResponse;
import com.education.platform.dto.profile.ProfileAccessVerifyRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.ProfileAccessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile/verification")
public class ProfileVerificationController {

    private final CurrentUserService currentUserService;
    private final ProfileAccessService profileAccessService;

    public ProfileVerificationController(
            CurrentUserService currentUserService, ProfileAccessService profileAccessService) {
        this.currentUserService = currentUserService;
        this.profileAccessService = profileAccessService;
    }

    @PostMapping("/send-code")
    public MessageResponse sendCode() {
        profileAccessService.sendVerificationCode(currentUserService.getCurrentUser());
        return new MessageResponse("Un code à 6 chiffres a été envoyé à votre adresse e-mail.");
    }

    @PostMapping("/verify")
    public MessageResponse verify(@Valid @RequestBody ProfileAccessVerifyRequest request) {
        profileAccessService.verifyCode(currentUserService.getCurrentUser(), request.getCode().trim());
        return new MessageResponse("Vérification réussie.");
    }
}
