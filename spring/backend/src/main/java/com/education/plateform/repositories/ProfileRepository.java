package com.education.plateform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.education.plateform.entities.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
