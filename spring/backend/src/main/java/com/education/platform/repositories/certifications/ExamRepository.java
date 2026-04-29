package com.education.platform.repositories.certifications;

import com.education.platform.entities.certifications.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCertificationId(Long certificationId);
}