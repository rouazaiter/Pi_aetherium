package com.education.platform.repositories.cv;

import com.education.platform.entities.cv.CVProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CVProfileRepository extends JpaRepository<CVProfile, Long> {

    Optional<CVProfile> findByUser_Id(Long userId);
}
