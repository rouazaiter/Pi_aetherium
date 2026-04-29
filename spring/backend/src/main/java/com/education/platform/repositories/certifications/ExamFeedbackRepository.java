package com.education.platform.repositories.certifications;

import com.education.platform.entities.certifications.ExamFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamFeedbackRepository extends JpaRepository<ExamFeedback, Long> {
    List<ExamFeedback> findByCertificationId(Long certificationId);
    List<ExamFeedback> findByUserIdentifier(String userIdentifier);
    Optional<ExamFeedback> findByEnrollmentId(Long enrollmentId);
    boolean existsByEnrollmentId(Long enrollmentId);
}
