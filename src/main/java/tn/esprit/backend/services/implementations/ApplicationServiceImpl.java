package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.NotificationDto;
import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.ApplicationStatus;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.entities.User;
import tn.esprit.backend.repositories.ApplicationRepository;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.repositories.UserRepository;
import tn.esprit.backend.services.interfaces.ApplicationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Application createApplication(Long applicantId, Long serviceRequestId, String message) {
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + applicantId));

        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRequest not found: " + serviceRequestId));

        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Applications are accepted only when service request is OPEN");
        }

        boolean alreadyApplied = applicationRepository.findByServiceRequestAndApplicant(serviceRequest, applicant).isPresent();
        if (alreadyApplied) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already applied to this service request");
        }

        Application application = Application.builder()
                .message(message)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .serviceRequest(serviceRequest)
                .applicant(applicant)
                .build();
////////////////////////////////////
        Application saved = applicationRepository.save(application);

        Long creatorId = serviceRequest.getCreator().getId();
        notificationService.notifyUser(
                creatorId,
                new NotificationDto(
                        "NEW_APPLICATION",
                        "Nouvelle candidature sur ta demande " + serviceRequest.getName(),
                        serviceRequest.getId(),
                        saved.getId(),
                        LocalDateTime.now()
                )
        );

        return saved;
        /////////////////////////////////////////
    }

    @Override
    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        return fetchApplication(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByServiceRequest(Long serviceRequestId, Long requesterId) {
        ServiceRequest serviceRequest = fetchServiceRequest(serviceRequestId);
        ensureCreator(serviceRequest, requesterId);
        return applicationRepository.findByServiceRequest(serviceRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByUser(Long applicantId) {
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + applicantId));
        return applicationRepository.findByApplicant(applicant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByServiceRequestAndStatus(Long serviceRequestId, ApplicationStatus status, Long requesterId) {
        ServiceRequest serviceRequest = fetchServiceRequest(serviceRequestId);
        ensureCreator(serviceRequest, requesterId);
        return applicationRepository.findByServiceRequestAndStatus(serviceRequest, status);
    }

    @Override
    @Transactional
    public Application updateApplicationStatus(Long applicationId, Long requesterId, ApplicationStatus status) {
        if (status == ApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status update to PENDING is not allowed");
        }

        Application application = fetchApplication(applicationId);
        ServiceRequest serviceRequest = application.getServiceRequest();
        ensureCreator(serviceRequest, requesterId);

        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update application when service request is not OPEN");
        }

        application.setStatus(status);
        Application saved = applicationRepository.save(application);

        if (status == ApplicationStatus.ACCEPTED) {
            serviceRequest.setStatus(ServiceRequestStatus.CLOSED);
            serviceRequest.setUpdatedAt(LocalDateTime.now());
            serviceRequestRepository.save(serviceRequest);
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserApplied(Long serviceRequestId, Long applicantId) {
        ServiceRequest serviceRequest = fetchServiceRequest(serviceRequestId);
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + applicantId));
        return applicationRepository.findByServiceRequestAndApplicant(serviceRequest, applicant).isPresent();
    }

    private Application fetchApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + id));
    }

    private ServiceRequest fetchServiceRequest(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRequest not found: " + id));
    }

    private void ensureCreator(ServiceRequest serviceRequest, Long requesterId) {
        if (!serviceRequest.getCreator().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only service request creator can perform this action");
        }
    }

}
