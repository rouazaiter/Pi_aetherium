package com.education.platform.repositories;

import com.education.platform.entities.AiJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiJobRepository extends JpaRepository<AiJob, Long> {
}