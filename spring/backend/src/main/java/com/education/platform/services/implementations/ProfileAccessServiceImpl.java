package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.entities.ProfileAccessCode;
import com.education.platform.entities.User;
import com.education.platform.repositories.ProfileAccessCodeRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.ProfileAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class ProfileAccessServiceImpl implements ProfileAccessService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration SESSION_TTL = Duration.ofMinutes(45);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProfileAccessCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMailService accountMailService;

    public ProfileAccessServiceImpl(
            ProfileAccessCodeRepository codeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AccountMailService accountMailService) {
        this.codeRepository = codeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountMailService = accountMailService;
    }

    @Override
    public void requireProfileAccess(User user) {
        Instant until = user.getProfileAccessValidUntil();
        if (until == null || until.isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PROFILE_VERIFICATION_REQUIRED");
        }
    }

    @Override
    @Transactional
    public void sendVerificationCode(User user) {
        Optional<ProfileAccessCode> existing = codeRepository.findByUser_Id(user.getId());
        if (existing.isPresent()) {
            Instant next = existing.get().getSentAt().plus(RESEND_COOLDOWN);
            if (next.isAfter(Instant.now())) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Réessayez dans une minute.");
            }
        }
        codeRepository.deleteByUser_Id(user.getId());

        int codeNum = 100_000 + RANDOM.nextInt(900_000);
        String plain = String.valueOf(codeNum);
        Instant now = Instant.now();
        ProfileAccessCode entity =
                ProfileAccessCode.builder()
                        .user(userRepository.getReferenceById(user.getId()))
                        .codeHash(passwordEncoder.encode(plain))
                        .expiresAt(now.plus(CODE_TTL))
                        .sentAt(now)
                        .attempts(0)
                        .build();
        codeRepository.save(entity);
        accountMailService.sendProfileAccessCode(user.getEmail(), plain);
    }

    @Override
    @Transactional
    public void verifyCode(User user, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Code invalide.");
        }
        ProfileAccessCode pac =
                codeRepository
                        .findByUser_Id(user.getId())
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Aucun code actif. Demandez un nouvel envoi."));
        if (pac.getExpiresAt().isBefore(Instant.now())) {
            codeRepository.delete(pac);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ce code a expiré. Demandez un nouvel envoi.");
        }
        if (pac.getAttempts() >= MAX_ATTEMPTS) {
            codeRepository.delete(pac);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Trop de tentatives. Demandez un nouveau code.");
        }
        if (!passwordEncoder.matches(code, pac.getCodeHash())) {
            pac.setAttempts(pac.getAttempts() + 1);
            codeRepository.save(pac);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Code incorrect.");
        }
        codeRepository.delete(pac);
        User fresh = userRepository.findById(user.getId()).orElseThrow();
        fresh.setProfileAccessValidUntil(Instant.now().plus(SESSION_TTL));
        userRepository.save(fresh);
    }
}
