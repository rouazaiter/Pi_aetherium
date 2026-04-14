package tn.esprit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.MeetingReservation;
import tn.esprit.backend.entities.ServiceRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingReservationRepository extends JpaRepository<MeetingReservation, Long> {
    Optional<MeetingReservation> findByApplication(Application application);
    List<MeetingReservation> findByServiceRequest(ServiceRequest serviceRequest);
}
