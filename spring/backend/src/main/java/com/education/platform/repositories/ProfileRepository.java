package com.education.platform.repositories;

import com.education.platform.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser_Id(Long userId);

    /** Pour « mot de passe oublié » : l’utilisateur peut saisir l’e-mail de récupération enregistré sur le profil. */
    Optional<Profile> findFirstByRecuperationEmailIgnoreCase(String recuperationEmail);
}
