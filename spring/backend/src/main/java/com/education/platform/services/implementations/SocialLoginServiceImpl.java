package com.education.platform.services.implementations;

import com.education.platform.dto.auth.AuthResponse;
import com.education.platform.dto.auth.SocialLoginRequest;
import com.education.platform.dto.auth.SocialProvider;
import com.education.platform.entities.User;
import com.education.platform.oauth.FacebookOAuthClient;
import com.education.platform.oauth.GoogleIdTokenService;
import com.education.platform.services.interfaces.AuthService;
import com.education.platform.services.interfaces.SocialLoginService;
import com.education.platform.services.interfaces.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialLoginServiceImpl implements SocialLoginService {

    private final GoogleIdTokenService googleIdTokenService;
    private final FacebookOAuthClient facebookOAuthClient;
    private final UserService userService;
    private final AuthService authService;

    public SocialLoginServiceImpl(
            GoogleIdTokenService googleIdTokenService,
            FacebookOAuthClient facebookOAuthClient,
            UserService userService,
            AuthService authService
    ) {
        this.googleIdTokenService = googleIdTokenService;
        this.facebookOAuthClient = facebookOAuthClient;
        this.userService = userService;
        this.authService = authService;
    }

    @Override
    @Transactional
    public AuthResponse login(SocialLoginRequest request) {
        User user;
        if (request.getProvider() == SocialProvider.GOOGLE) {
            var claims = googleIdTokenService.verifyAndExtract(request.getToken());
            user = userService.provisionFromGoogle(
                    claims.subject(),
                    claims.email(),
                    claims.emailVerified(),
                    claims.givenName(),
                    claims.familyName()
            );
        } else if (request.getProvider() == SocialProvider.FACEBOOK) {
            var claims = facebookOAuthClient.validateAndFetchUser(request.getToken());
            user = userService.provisionFromFacebook(
                    claims.id(),
                    claims.email(),
                    claims.firstName(),
                    claims.lastName()
            );
        } else {
            throw new IllegalArgumentException("Fournisseur inconnu");
        }
        return authService.toAuthResponse(user);
    }
}
