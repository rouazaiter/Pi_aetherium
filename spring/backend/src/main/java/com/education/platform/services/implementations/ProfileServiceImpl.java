package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.profile.ProfileResponse;
import com.education.platform.dto.profile.ProfileUpdateRequest;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.repositories.ProfileRepository;
import com.education.platform.services.interfaces.ProfileAccessService;
import com.education.platform.services.interfaces.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileAccessService profileAccessService;
    private final ProfilePictureStorage profilePictureStorage;

    public ProfileServiceImpl(
            ProfileRepository profileRepository,
            ProfileAccessService profileAccessService,
            ProfilePictureStorage profilePictureStorage) {
        this.profileRepository = profileRepository;
        this.profileAccessService = profileAccessService;
        this.profilePictureStorage = profilePictureStorage;
    }

    @Override
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

    private static ProfileResponse toResponse(Profile p) {
        return ProfileResponse.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .interests(p.getInterests())
                .description(p.getDescription())
                .profilePicture(p.getProfilePicture())
                .lastPasswordChanged(p.getLastPasswordChanged())
                .recuperationEmail(p.getRecuperationEmail())
                .build();
    }
}
