package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.auth.SignUpRequest;
import com.education.platform.entities.AccountStatus;
import com.education.platform.entities.Profile;
import com.education.platform.entities.Role;
import com.education.platform.entities.User;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce nom d'utilisateur est déjà utilisé");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Cet e-mail est déjà utilisé");
        }
        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        if (role == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Le rôle admin ne peut pas être choisi à l'inscription");
        }
        Instant now = Instant.now();
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .createdAt(now)
                .role(role)
                .accountStatus(AccountStatus.ACTIVE)
                .subscriptions(new ArrayList<>())
                .build();
        Profile profile = Profile.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .description(request.getDescription())
                .recuperationEmail(request.getRecuperationEmail())
                .interests(request.getInterests() != null ? new ArrayList<>(request.getInterests()) : new ArrayList<>())
                .lastPasswordChanged(now)
                .user(user)
                .build();
        user.setProfile(profile);
        return userRepository.save(user);
    }

    @Override
    public User requireByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    @Override
    @Transactional
    public User provisionFromGoogle(String googleSub, String email, boolean emailVerified, String givenName, String familyName) {
        if (!emailVerified) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "E-mail Google non vérifié");
        }
        Optional<User> byGoogle = userRepository.findByGoogleId(googleSub);
        if (byGoogle.isPresent()) {
            return touchLastLogin(byGoogle.get());
        }
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User u = byEmail.get();
            if (u.getGoogleId() != null && !u.getGoogleId().equals(googleSub)) {
                throw new ApiException(HttpStatus.CONFLICT, "Ce compte est déjà lié à un autre compte Google");
            }
            u.setGoogleId(googleSub);
            return touchLastLogin(userRepository.save(u));
        }
        return createSocialUser(email, givenName, familyName, googleSub, null);
    }

    @Override
    @Transactional
    public User provisionFromFacebook(String facebookUserId, String email, String firstName, String lastName) {
        Optional<User> byFb = userRepository.findByFacebookId(facebookUserId);
        if (byFb.isPresent()) {
            return touchLastLogin(byFb.get());
        }
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User u = byEmail.get();
            if (u.getFacebookId() != null && !u.getFacebookId().equals(facebookUserId)) {
                throw new ApiException(HttpStatus.CONFLICT, "Ce compte est déjà lié à un autre compte Facebook");
            }
            u.setFacebookId(facebookUserId);
            return touchLastLogin(userRepository.save(u));
        }
        return createSocialUser(email, firstName, lastName, null, facebookUserId);
    }

    private User touchLastLogin(User user) {
        user.setLastLogin(Instant.now());
        return userRepository.save(user);
    }

    private User createSocialUser(String email, String firstName, String lastName, String googleId, String facebookId) {
        Instant now = Instant.now();
        String username = generateUniqueUsername(email);
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .createdAt(now)
                .role(Role.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .subscriptions(new ArrayList<>())
                .googleId(googleId)
                .facebookId(facebookId)
                .build();
        Profile profile = Profile.builder()
                .firstName(firstName)
                .lastName(lastName)
                .interests(new ArrayList<>())
                .lastPasswordChanged(now)
                .user(user)
                .build();
        user.setProfile(profile);
        return userRepository.save(user);
    }

    private String generateUniqueUsername(String email) {
        int at = email.indexOf('@');
        String local = at > 0 ? email.substring(0, at) : email;
        String base = local.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (base.length() < 3) {
            base = "user_" + base;
        }
        base = base.substring(0, Math.min(64, base.length()));
        String candidate = base;
        int i = 0;
        while (userRepository.existsByUsername(candidate)) {
            String suffix = "_" + (++i);
            int maxBase = 64 - suffix.length();
            candidate = base.substring(0, Math.min(maxBase, base.length())) + suffix;
        }
        return candidate;
    }
}
