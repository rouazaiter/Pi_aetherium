package com.education.platform.repositories.certifications;

import com.education.platform.entities.certifications.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserIdentifierAndCertificationId(String userIdentifier, Long certificationId);
    List<Enrollment> findByUserIdentifier(String userIdentifier);
    boolean existsByUserIdentifierAndCertificationId(String userIdentifier, Long certificationId);
    List<Enrollment> findByCertificationId(Long certificationId);

    /** Find by certificate ID string, e.g. "SKH-000017" → id = 17 */
    default Optional<Enrollment> findByCertificateId(String certId) {
        try {
            // certId format: SKH-XXXXXX  (zero-padded 6 digits)
            String numeric = certId.replaceAll("[^0-9]", "");
            if (numeric.isBlank()) return Optional.empty();
            return findById(Long.parseLong(numeric));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
