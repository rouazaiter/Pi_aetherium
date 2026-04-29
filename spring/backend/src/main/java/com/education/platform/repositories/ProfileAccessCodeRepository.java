package com.education.platform.repositories;

import com.education.platform.entities.ProfileAccessCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileAccessCodeRepository extends JpaRepository<ProfileAccessCode, Long> {

    Optional<ProfileAccessCode> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
}
