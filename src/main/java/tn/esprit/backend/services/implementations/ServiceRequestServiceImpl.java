package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestCategory;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.entities.User;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.repositories.UserRepository;
import tn.esprit.backend.services.interfaces.ServiceRequestService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ServiceRequest createServiceRequest(Long creatorId, ServiceRequest serviceRequest) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + creatorId));

        LocalDateTime now = LocalDateTime.now();
        serviceRequest.setId(null);

        if (serviceRequest.getCategory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service request category is required");
        }

        serviceRequest.setCreator(creator);
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        serviceRequest.setCreatedAt(now);
        serviceRequest.setUpdatedAt(now);
        return serviceRequestRepository.save(serviceRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequest getServiceRequestById(Long id) {
        return fetchServiceRequest(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequest> getAllServiceRequests() {
        return serviceRequestRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequest> getServiceRequestsByStatus(ServiceRequestStatus status) {
        return serviceRequestRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequest> getServiceRequestsByUser(Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        return serviceRequestRepository.findByCreator(creator);
    }

    @Override
    @Transactional
    public ServiceRequest updateServiceRequest(Long id, Long requesterId, ServiceRequest payload) {
        ServiceRequest serviceRequest = fetchServiceRequest(id);
        ensureCreator(serviceRequest, requesterId);
        ensureUpdatable(serviceRequest);

        if (payload.getName() != null && !payload.getName().isBlank()) {
            serviceRequest.setName(payload.getName());
        }
        if (payload.getDescription() != null) {
            serviceRequest.setDescription(payload.getDescription());
        }
        if (payload.getCategory() != null) {
            serviceRequest.setCategory(payload.getCategory());
        }
        if (payload.getFiles() != null) {
            serviceRequest.setFiles(payload.getFiles());
        }
        if (payload.getExpiringDate() != null) {
            serviceRequest.setExpiringDate(payload.getExpiringDate());
        }
        serviceRequest.setUpdatedAt(LocalDateTime.now());
        return serviceRequestRepository.save(serviceRequest);
    }

    @Override
    @Transactional
    public void deleteServiceRequest(Long id, Long requesterId) {
        ServiceRequest serviceRequest = fetchServiceRequest(id);
        ensureCreator(serviceRequest, requesterId);
        serviceRequestRepository.delete(serviceRequest);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public int checkAndUpdateExpiredRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<ServiceRequest> expiredRequests = serviceRequestRepository.findExpiredRequests(now);
        expiredRequests.forEach(sr -> {
            sr.setStatus(ServiceRequestStatus.EXPIRED);
            sr.setUpdatedAt(now);
        });
        serviceRequestRepository.saveAll(expiredRequests);
        return expiredRequests.size();
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

    private void ensureUpdatable(ServiceRequest serviceRequest) {
        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only OPEN service requests can be updated");
        }
    }

}
