package tn.esprit.backend.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.entities.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByCreator(User creator);
    List<ServiceRequest> findByCreator_Id(Long creatorId);
    List<ServiceRequest> findByStatus(ServiceRequestStatus status);
    List<ServiceRequest> findByStatusAndExpiringDateAfter(ServiceRequestStatus status, LocalDateTime now);
    List<ServiceRequest> findByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.creator.id = :viewerId OR sr.status = tn.esprit.backend.entities.ServiceRequestStatus.OPEN ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findVisibleForUser(@Param("viewerId") Long viewerId);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.status <> tn.esprit.backend.entities.ServiceRequestStatus.EXPIRED AND sr.expiringDate IS NOT NULL AND sr.expiringDate < :now")
    List<ServiceRequest> findExpiredRequests(@Param("now") LocalDateTime now);
}
