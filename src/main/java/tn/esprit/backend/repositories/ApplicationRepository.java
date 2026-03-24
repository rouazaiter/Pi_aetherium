package tn.esprit.backend.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.ApplicationStatus;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByServiceRequestAndApplicant(ServiceRequest serviceRequest, User applicant);
    List<Application> findByApplicant(User applicant);
    List<Application> findByServiceRequest(ServiceRequest serviceRequest);
    List<Application> findByServiceRequestAndStatus(ServiceRequest serviceRequest, ApplicationStatus status);
}
