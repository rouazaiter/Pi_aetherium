package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.auth.AuthResponse;
import com.education.platform.dto.auth.LoginRequest;
import com.education.platform.entities.LoginActivity;
import com.education.platform.entities.User;
import com.education.platform.repositories.LoginActivityRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.security.JwtService;
import com.education.platform.services.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LoginActivityRepository loginActivityRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            LoginActivityRepository loginActivityRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.loginActivityRepository = loginActivityRepository;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identifiant ou mot de passe incorrect");
        }
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Identifiant ou mot de passe incorrect"));
        user.setLastLogin(Instant.now());
        userRepository.save(user);
        logLoginActivity(user);
        return toAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse toAuthResponse(User user) {
        user.setProfileAccessValidUntil(null);
        userRepository.save(user);
        String token = jwtService.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        String profilePic = null;
        if (user.getProfile() != null) {
            profilePic = user.getProfile().getProfilePicture();
        }
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePicture(profilePic)
                .build();
    }

    private void logLoginActivity(User user) {
        String ipAddress = null;
        String userAgent = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest servletRequest = attributes.getRequest();
            ipAddress = servletRequest.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = servletRequest.getRemoteAddr();
            }
            userAgent = servletRequest.getHeader("User-Agent");
        }
        LoginActivity activity = LoginActivity.builder()
                .user(user)
                .loggedAt(Instant.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        loginActivityRepository.save(activity);
    }
}
