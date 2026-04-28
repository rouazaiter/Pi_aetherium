package com.education.plateform.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.education.plateform.entities.Application;
import com.education.plateform.entities.ApplicationStatus;
import com.education.plateform.entities.ServiceRequest;
import com.education.plateform.entities.User;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByServiceRequestAndApplicant(ServiceRequest serviceRequest, User applicant);
    List<Application> findByApplicant(User applicant);
    List<Application> findByServiceRequest(ServiceRequest serviceRequest);
    List<Application> findByServiceRequestAndStatus(ServiceRequest serviceRequest, ApplicationStatus status);
    List<Application> findByAppliedAtGreaterThanEqual(LocalDateTime appliedAt);
}
