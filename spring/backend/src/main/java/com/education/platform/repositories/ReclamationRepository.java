package com.education.platform.repositories;

import com.education.platform.entities.Reclamation;
import com.education.platform.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    List<Reclamation> findByUserOrderByCreatedAtDesc(User user);

    Optional<Reclamation> findByIdAndUser(Long id, User user);

    List<Reclamation> findAllByOrderByCreatedAtDesc();
}
