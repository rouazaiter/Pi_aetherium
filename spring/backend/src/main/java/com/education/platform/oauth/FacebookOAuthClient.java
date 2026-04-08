package com.education.platform.oauth;

import com.education.platform.common.ApiException;
import com.education.platform.config.OAuthClientProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FacebookOAuthClient {

    private final OAuthClientProperties oauth;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public FacebookOAuthClient(OAuthClientProperties oauth, ObjectMapper objectMapper) {
        this.oauth = oauth;
        this.objectMapper = objectMapper;
    }

    public FacebookUserClaims validateAndFetchUser(String userAccessToken) {
        if (!StringUtils.hasText(oauth.getFacebookAppId()) || !StringUtils.hasText(oauth.getFacebookAppSecret())) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Connexion Facebook non configurée sur le serveur");
        }
        if (!StringUtils.hasText(userAccessToken)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Jeton Facebook manquant");
        }
        String appAccessToken = oauth.getFacebookAppId() + "|" + oauth.getFacebookAppSecret();
        String debugUri = UriComponentsBuilder.fromUriString("https://graph.facebook.com/debug_token")
                .queryParam("input_token", userAccessToken)
                .queryParam("access_token", appAccessToken)
                .encode()
                .build()
                .toUriString();

        String debugBody = restClient.get()
                .uri(debugUri)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(debugBody);
            JsonNode data = root.path("data");
            if (!data.path("is_valid").asBoolean(false)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Jeton Facebook invalide");
            }
            if (!oauth.getFacebookAppId().equals(data.path("app_id").asText())) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Jeton Facebook pour une autre application");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Impossible de valider le jeton Facebook");
        }

        String meUri = UriComponentsBuilder.fromUriString("https://graph.facebook.com/v19.0/me")
                .queryParam("fields", "id,email,first_name,last_name")
                .queryParam("access_token", userAccessToken)
                .encode()
                .build()
                .toUriString();

        String meBody = restClient.get()
                .uri(meUri)
                .retrieve()
                .body(String.class);
        try {
            JsonNode me = objectMapper.readTree(meBody);
            if (me.has("error")) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Impossible de lire le profil Facebook");
            }
            String id = me.path("id").asText(null);
            String email = me.path("email").asText(null);
            if (!StringUtils.hasText(id)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Identifiant Facebook manquant");
            }
            if (!StringUtils.hasText(email)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Autorisez l’accès à l’e-mail Facebook (portée email) pour vous connecter"
                );
            }
            return new FacebookUserClaims(
                    id,
                    email,
                    me.path("first_name").asText(null),
                    me.path("last_name").asText(null)
            );
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Impossible de lire le profil Facebook");
        }
    }
}
