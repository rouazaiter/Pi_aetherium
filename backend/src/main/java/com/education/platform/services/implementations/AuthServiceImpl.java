package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.auth.AuthResponse;
import com.education.platform.dto.auth.LoginRequest;
import com.education.platform.entities.User;
import com.education.platform.repositories.UserRepository;
import com.education.platform.security.JwtService;
import com.education.platform.services.interfaces.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
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
        return toAuthResponse(user);
    }

    @Override
    public AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
