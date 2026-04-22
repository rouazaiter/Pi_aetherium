package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.entities.PasswordResetToken;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.repositories.PasswordResetTokenRepository;
import com.education.platform.repositories.ProfileRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private final ProfileRepository profileRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMailService accountMailService;

    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            AccountMailService accountMailService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
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
            opt = profileRepository
                    .findFirstByRecuperationEmailIgnoreCase(normalized)
                    .map(Profile::getUser);
        }
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
        String deliveryEmail = resolvePasswordResetDeliveryEmail(user);
        if (!StringUtils.hasText(deliveryEmail)) {
            return;
        }
        accountMailService.sendPasswordReset(deliveryEmail, resetUrl);
    }

    /**
     * Envoie le lien sur l’e-mail de récupération du profil s’il est renseigné (inscription ou profil),
     * sinon sur l’e-mail du compte pour compatibilité avec les anciens comptes.
     */
    private String resolvePasswordResetDeliveryEmail(User user) {
        Profile profile = user.getProfile();
        if (profile != null && StringUtils.hasText(profile.getRecuperationEmail())) {
            return profile.getRecuperationEmail().trim().toLowerCase(Locale.ROOT);
        }
        if (user.getEmail() != null) {
            return user.getEmail().trim().toLowerCase(Locale.ROOT);
        }
        return null;
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
