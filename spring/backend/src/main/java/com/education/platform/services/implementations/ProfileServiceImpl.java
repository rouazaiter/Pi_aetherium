package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.profile.LoginActivityResponse;
import com.education.platform.dto.profile.ProfileResponse;
import com.education.platform.dto.profile.ProfileUpdateRequest;
import com.education.platform.entities.LoginActivity;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.repositories.LoginActivityRepository;
import com.education.platform.repositories.ProfileRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.ProfileAccessService;
import com.education.platform.services.interfaces.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final LoginActivityRepository loginActivityRepository;
    private final UserRepository userRepository;
    private final ProfileAccessService profileAccessService;
    private final ProfilePictureStorage profilePictureStorage;

    public ProfileServiceImpl(
            ProfileRepository profileRepository,
            LoginActivityRepository loginActivityRepository,
            UserRepository userRepository,
            ProfileAccessService profileAccessService,
            ProfilePictureStorage profilePictureStorage) {
        this.profileRepository = profileRepository;
        this.loginActivityRepository = loginActivityRepository;
        this.userRepository = userRepository;
        this.profileAccessService = profileAccessService;
        this.profilePictureStorage = profilePictureStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getForUser(User user) {
        profileAccessService.requireProfileAccess(user);
        Profile p = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profil introuvable"));
        return toResponse(p);
    }

    @Override
    @Transactional
    public ProfileResponse updateForUser(User user, ProfileUpdateRequest request) {
        profileAccessService.requireProfileAccess(user);
        Profile p = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profil introuvable"));
        if (request.getFirstName() != null) {
            p.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            p.setLastName(request.getLastName());
        }
        if (request.getInterests() != null) {
            p.getInterests().clear();
            p.getInterests().addAll(request.getInterests());
        }
        if (request.getDescription() != null) {
            p.setDescription(request.getDescription());
        }
        if (request.getProfilePicture() != null) {
            p.setProfilePicture(request.getProfilePicture());
        }
        if (request.getRecuperationEmail() != null) {
            p.setRecuperationEmail(request.getRecuperationEmail());
        }
        if (request.getTwoFactorEnabled() != null) {
            p.setTwoFactorEnabled(request.getTwoFactorEnabled());
        }
        if (request.getActiveStatusVisible() != null) {
            p.setActiveStatusVisible(request.getActiveStatusVisible());
        }
        return toResponse(p);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfilePhoto(User user, MultipartFile file) {
        profileAccessService.requireProfileAccess(user);
        Profile p = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profil introuvable"));
        try {
            profilePictureStorage.deleteIfManagedByUs(p.getProfilePicture());
            String url = profilePictureStorage.store(user.getId(), file);
            p.setProfilePicture(url);
            profileRepository.save(p);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Enregistrement de l’image impossible.");
        }
        return toResponse(p);
    }

    @Override
    @Transactional
    public ProfileResponse updateTwoFactorForUser(User user, boolean enabled) {
        profileAccessService.requireProfileAccess(user);
        Profile p = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profil introuvable"));
        p.setTwoFactorEnabled(enabled);
        // Force a fresh e-mail verification the next time protected profile access is checked.
        if (enabled) {
            User fresh = userRepository.findById(user.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
            fresh.setProfileAccessValidUntil(null);
            userRepository.save(fresh);
        }
        return toResponse(p);
    }

    @Override
    public List<LoginActivityResponse> getLoginActivityForUser(User user) {
        profileAccessService.requireProfileAccess(user);
        List<LoginActivity> activities = loginActivityRepository.findTop20ByUser_IdOrderByLoggedAtDesc(user.getId());
        return activities.stream()
                .map(activity -> LoginActivityResponse.builder()
                        .loggedAt(activity.getLoggedAt())
                        .ipAddress(activity.getIpAddress())
                        .userAgent(activity.getUserAgent())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ProfileResponse updateActiveStatusVisibilityForUser(User user, boolean enabled) {
        profileAccessService.requireProfileAccess(user);
        Profile p = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profil introuvable"));
        p.setActiveStatusVisible(enabled);
        return toResponse(p);
    }

    private static ProfileResponse toResponse(Profile p) {
        List<String> interestsSafe = p.getInterests() != null
                ? new ArrayList<>(p.getInterests())
                : Collections.emptyList();
        return ProfileResponse.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .interests(interestsSafe)
                .description(p.getDescription())
                .profilePicture(p.getProfilePicture())
                .lastPasswordChanged(p.getLastPasswordChanged())
                .recuperationEmail(p.getRecuperationEmail())
                .twoFactorEnabled(p.isTwoFactorEnabled())
                .activeStatusVisible(p.isActiveStatusVisible())
                .build();
    }
}
