package tn.esprit.backend.services.interfaces;

import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.ApplicationStatus;

import java.util.List;

public interface ApplicationService {
    Application createApplication(Long applicantId, Long serviceRequestId, String message);
    Application getApplicationById(Long id);
    List<Application> getApplicationsByServiceRequest(Long serviceRequestId, Long requesterId);
    List<Application> getApplicationsByUser(Long applicantId);
    List<Application> getApplicationsByServiceRequestAndStatus(Long serviceRequestId, ApplicationStatus status, Long requesterId);
    Application updateApplicationStatus(Long applicationId, Long requesterId, ApplicationStatus status);
    Application updateApplicationMessage(Long applicationId, Long applicantId, String message);
    boolean hasUserApplied(Long serviceRequestId, Long applicantId);
}
