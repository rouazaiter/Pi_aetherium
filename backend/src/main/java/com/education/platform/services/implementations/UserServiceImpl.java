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
}
