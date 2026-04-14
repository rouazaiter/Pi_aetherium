package tn.esprit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.backend.entities.MeetingConfig;
import tn.esprit.backend.entities.ServiceRequest;

import java.util.Optional;

@Repository
public interface MeetingConfigRepository extends JpaRepository<MeetingConfig, Long> {
    Optional<MeetingConfig> findByServiceRequest(ServiceRequest serviceRequest);
}
