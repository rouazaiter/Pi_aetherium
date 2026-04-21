package tn.esprit.backend.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.backend.dto.DashboardEventProjection;
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
    List<ServiceRequest> findAllByOrderByCreatedAtDesc();

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.creator.id = :viewerId OR sr.status = tn.esprit.backend.entities.ServiceRequestStatus.OPEN ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findVisibleForUser(@Param("viewerId") Long viewerId);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.status <> tn.esprit.backend.entities.ServiceRequestStatus.EXPIRED AND sr.expiringDate IS NOT NULL AND sr.expiringDate < :now")
    List<ServiceRequest> findExpiredRequests(@Param("now") LocalDateTime now);

    @Query("""
            SELECT sr.id AS id,
                   sr.name AS eventName,
                   sr.createdAt AS eventDate,
                   sr.category AS category,
                   sr.status AS status,
                   sr.price AS amount,
                   COUNT(app.id) AS participants
            FROM ServiceRequest sr
            LEFT JOIN sr.applications app
            GROUP BY sr.id, sr.name, sr.createdAt, sr.category, sr.status, sr.price
            ORDER BY sr.createdAt DESC
            """)
    List<DashboardEventProjection> findDashboardEvents();
}
