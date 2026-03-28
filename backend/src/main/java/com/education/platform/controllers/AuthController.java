package com.education.platform.controllers;

import com.education.platform.dto.auth.AuthResponse;
import com.education.platform.dto.auth.LoginRequest;
import com.education.platform.dto.auth.SignUpRequest;
import com.education.platform.entities.User;
import com.education.platform.services.interfaces.AuthService;
import com.education.platform.services.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
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
}
