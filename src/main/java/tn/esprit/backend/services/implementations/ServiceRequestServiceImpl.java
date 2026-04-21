package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.CreateServiceRequestRequest;
import tn.esprit.backend.dto.ServiceRequestResponse;
import tn.esprit.backend.dto.UpdateServiceRequestRequest;
import tn.esprit.backend.dto.NotificationPriority;
import tn.esprit.backend.dto.UserSummaryResponse;
import tn.esprit.backend.entities.PaymentStatus;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestCategory;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.entities.User;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.repositories.UserRepository;
import tn.esprit.backend.services.interfaces.FileStorageService;
import tn.esprit.backend.services.interfaces.ServiceRequestService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ServiceRequestResponse createServiceRequest(Long creatorId, CreateServiceRequestRequest request, MultipartFile file) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + creatorId));

        LocalDateTime now = LocalDateTime.now();

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setName(request.name());
        serviceRequest.setCategory(request.category());
        serviceRequest.setDescription(request.description());
        serviceRequest.setExpiringDate(request.expiringDate());
        serviceRequest.setPrice(request.price());

        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.store(file);
            serviceRequest.setFiles("/api/service-requests/files/" + storedFileName);
        }

        serviceRequest.setCreator(creator);
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        serviceRequest.setCreatedAt(now);
        serviceRequest.setUpdatedAt(now);
        ServiceRequest saved = serviceRequestRepository.save(serviceRequest);

        List<Long> recipients = userRepository.findAll().stream()
            .map(User::getId)
            .filter(id -> !id.equals(creatorId))
            .toList();

        notificationService.notifyUsersWithAssistant(
            recipients,
            "NEW_SERVICE_REQUEST",
            "New service request available: " + saved.getName() + ".",
            creator.getUsername(),
            saved.getName(),
            NotificationPriority.LOW,
            "Open request details",
            saved.getId(),
            null
        );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getServiceRequestById(Long id, Long viewerId) {
        return toResponse(fetchVisibleServiceRequest(id, viewerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getAllServiceRequests(Long viewerId) {
        return serviceRequestRepository.findVisibleForUser(viewerId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getServiceRequestsByStatus(Long viewerId, ServiceRequestStatus status) {
        return serviceRequestRepository.findVisibleForUser(viewerId).stream()
                .filter(request -> request.getStatus() == status)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getServiceRequestsByUser(Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        return serviceRequestRepository.findByCreator(creator).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ServiceRequestResponse updateServiceRequest(Long id, Long requesterId, UpdateServiceRequestRequest payload, MultipartFile file) {
        ServiceRequest serviceRequest = fetchServiceRequest(id);
        ensureCreator(serviceRequest, requesterId);
        ensureUpdatable(serviceRequest);

        if (payload.name() != null && !payload.name().isBlank()) {
            serviceRequest.setName(payload.name());
        }
        if (payload.description() != null) {
            serviceRequest.setDescription(payload.description());
        }
        if (payload.category() != null) {
            serviceRequest.setCategory(payload.category());
        }
        if (payload.price() != null) {
            serviceRequest.setPrice(payload.price());
        }
        if (payload.expiringDate() != null) {
            serviceRequest.setExpiringDate(payload.expiringDate());
        }
        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.store(file);
            serviceRequest.setFiles("/api/service-requests/files/" + storedFileName);
        }
        serviceRequest.setUpdatedAt(LocalDateTime.now());
        return toResponse(serviceRequestRepository.save(serviceRequest));
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

    private ServiceRequest fetchVisibleServiceRequest(Long id, Long viewerId) {
        ServiceRequest serviceRequest = fetchServiceRequest(id);
        if (serviceRequest.getCreator() != null && serviceRequest.getCreator().getId().equals(viewerId)) {
            return serviceRequest;
        }
        // Les demandes non publiques sont visibles seulement par le créateur
        if (serviceRequest.getStatus() == ServiceRequestStatus.OPEN) {
            return serviceRequest;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRequest not found: " + id);
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

    private ServiceRequestResponse toResponse(ServiceRequest serviceRequest) {
        return new ServiceRequestResponse(
                serviceRequest.getId(),
                serviceRequest.getName(),
                serviceRequest.getCategory(),
                serviceRequest.getDescription(),
                serviceRequest.getStatus(),
                serviceRequest.getPrice(),
                serviceRequest.getFiles(),
                serviceRequest.getCreatedAt(),
                serviceRequest.getUpdatedAt(),
                serviceRequest.getExpiringDate(),
                toUserSummary(serviceRequest.getCreator())
        );
    }

    private UserSummaryResponse toUserSummary(User creator) {
        return new UserSummaryResponse(creator.getId(), creator.getUsername(), creator.getEmail());
    }

}
