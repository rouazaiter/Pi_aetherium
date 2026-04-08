package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.entities.PasswordResetToken;
import com.education.platform.entities.User;
import com.education.platform.repositories.PasswordResetTokenRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMailService accountMailService;

    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            AccountMailService accountMailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountMailService = accountMailService;
    }

    @Override
    @Transactional
    public void requestReset(String email) {
        if (email == null) {
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return;
        }
        Optional<User> opt = userRepository.findByEmail(normalized);
        if (opt.isEmpty()) {
            return;
        }
        User user = opt.get();
        tokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        PasswordResetToken entity =
                PasswordResetToken.builder()
                        .user(user)
                        .token(token)
                        .expiresAt(Instant.now().plus(TOKEN_TTL))
                        .used(false)
                        .build();
        tokenRepository.save(entity);
        String base = frontendBaseUrl.trim().replaceAll("/+$", "");
        String resetUrl =
                base + "/reset-password?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        accountMailService.sendPasswordReset(user.getEmail(), resetUrl);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Lien invalide ou expiré.");
        }
        PasswordResetToken prt =
                tokenRepository
                        .findByToken(token.trim())
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Lien invalide ou expiré."));
        if (prt.isUsed() || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Lien invalide ou expiré.");
        }
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        if (user.getProfile() != null) {
            user.getProfile().setLastPasswordChanged(Instant.now());
        }
        prt.setUsed(true);
        tokenRepository.save(prt);
        userRepository.save(user);
    }
}
