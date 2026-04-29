package com.education.platform.oauth;

import com.education.platform.common.ApiException;
import com.education.platform.config.OAuthClientProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
public class GoogleIdTokenService {

    private final OAuthClientProperties oauth;

    public GoogleIdTokenService(OAuthClientProperties oauth) {
        this.oauth = oauth;
    }

    public GoogleUserClaims verifyAndExtract(String idTokenJwt) {
        if (!StringUtils.hasText(oauth.getGoogleClientId())) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Connexion Google non configurée sur le serveur");
        }
        if (!StringUtils.hasText(idTokenJwt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Jeton Google manquant");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(oauth.getGoogleClientId())).build();

            GoogleIdToken idToken = verifier.verify(idTokenJwt);
            if (idToken == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Jeton Google invalide");
            }
            var payload = idToken.getPayload();
            String sub = payload.getSubject();
            String email = payload.getEmail();
            Boolean verified = payload.getEmailVerified();
            if (!StringUtils.hasText(email)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "E-mail non fourni par Google");
            }
            return new GoogleUserClaims(
                    sub,
                    email,
                    Boolean.TRUE.equals(verified),
                    (String) payload.get("given_name"),
                    (String) payload.get("family_name")
            );
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Impossible de valider le jeton Google");
        }
    }
}
