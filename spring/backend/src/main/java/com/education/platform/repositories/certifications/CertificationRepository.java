package com.education.platform.repositories.certifications;

import com.education.platform.entities.certifications.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByTitleContainingIgnoreCase(String title);
    List<Certification> findByCategoryIgnoreCase(String category);
    List<Certification> findByStatus(Certification.Status status);
}