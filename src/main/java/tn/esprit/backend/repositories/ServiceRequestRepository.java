package tn.esprit.backend.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.entities.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByCreator(User creator);
    List<ServiceRequest> findByStatus(ServiceRequestStatus status);
    List<ServiceRequest> findByStatusAndExpiringDateAfter(ServiceRequestStatus status, LocalDateTime now);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.status <> tn.esprit.backend.entities.ServiceRequestStatus.EXPIRED AND sr.expiringDate IS NOT NULL AND sr.expiringDate < :now")
    List<ServiceRequest> findExpiredRequests(LocalDateTime now);
}
