package com.education.platform.controllers;

import com.education.platform.dto.auth.AuthResponse;
import com.education.platform.dto.auth.ForgotPasswordRequest;
import com.education.platform.dto.auth.LoginRequest;
import com.education.platform.dto.auth.MessageResponse;
import com.education.platform.dto.auth.ResetPasswordRequest;
import com.education.platform.dto.auth.SignUpRequest;
import com.education.platform.dto.auth.SocialLoginRequest;
import com.education.platform.entities.User;
import com.education.platform.services.interfaces.AuthService;
import com.education.platform.services.interfaces.PasswordResetService;
import com.education.platform.services.interfaces.SocialLoginService;
import com.education.platform.services.interfaces.UserService;

import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final SocialLoginService socialLoginService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            UserService userService,
            AuthService authService,
            SocialLoginService socialLoginService,
            PasswordResetService passwordResetService) {
        this.userService = userService;
        this.authService = authService;
        this.socialLoginService = socialLoginService;
        this.passwordResetService = passwordResetService;
    }

    /** Vérification rapide dans le navigateur : http://localhost:8081/api/auth/health */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "auth", "education-platform-backend");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody SignUpRequest request) {
        User user = userService.register(request);
        return authService.toAuthResponse(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/social")
    public AuthResponse social(@Valid @RequestBody SocialLoginRequest request) {
        return socialLoginService.login(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return new MessageResponse(
                "Si cette adresse est associée à un compte, un e-mail a été envoyé avec les instructions.");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return new MessageResponse("Votre mot de passe a été mis à jour. Vous pouvez vous connecter.");
    }
}
