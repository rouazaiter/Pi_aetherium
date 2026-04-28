package com.education.platform.repositories;
import com.education.platform.entities.PrivateDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivateDriveRepository extends JpaRepository<PrivateDrive, Long> {
    Optional<PrivateDrive> findByUserId(Long userId);
}
