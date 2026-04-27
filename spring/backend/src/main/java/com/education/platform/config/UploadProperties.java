package com.education.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.uploads")
public class UploadProperties {

    /** Répertoire local pour les photos de profil (relatif au répertoire de travail). */
    private String profilePicturesDir = "uploads/profile-pictures";

    /** Répertoire local pour les messages vocaux. */
    private String voiceMessagesDir = "uploads/voice-messages";
}
