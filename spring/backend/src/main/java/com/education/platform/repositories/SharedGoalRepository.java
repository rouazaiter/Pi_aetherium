package com.education.platform.repositories;

import com.education.platform.entities.SharedGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedGoalRepository extends JpaRepository<SharedGoal, Long> {

    List<SharedGoal> findByUser_IdOrderByUpdatedAtDesc(Long userId);
}
