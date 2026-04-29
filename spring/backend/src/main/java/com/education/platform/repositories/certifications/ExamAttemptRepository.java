package com.education.platform.repositories.certifications;

import com.education.platform.entities.certifications.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByEnrollmentId(Long enrollmentId);
    List<ExamAttempt> findByEnrollmentIdIn(java.util.Set<Long> enrollmentIds);
}
