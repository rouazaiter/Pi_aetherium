package tn.esprit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.backend.entities.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
