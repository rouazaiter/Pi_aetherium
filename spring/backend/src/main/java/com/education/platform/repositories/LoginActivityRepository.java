package com.education.platform.repositories;

import com.education.platform.entities.LoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginActivityRepository extends JpaRepository<LoginActivity, Long> {

    List<LoginActivity> findTop20ByUser_IdOrderByLoggedAtDesc(Long userId);
}
