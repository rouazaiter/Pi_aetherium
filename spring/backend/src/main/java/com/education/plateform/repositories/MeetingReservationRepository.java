package com.education.plateform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.education.plateform.entities.Application;
import com.education.plateform.entities.MeetingReservation;
import com.education.plateform.entities.ServiceRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingReservationRepository extends JpaRepository<MeetingReservation, Long> {
    Optional<MeetingReservation> findByApplication(Application application);
    List<MeetingReservation> findByServiceRequest(ServiceRequest serviceRequest);
}
