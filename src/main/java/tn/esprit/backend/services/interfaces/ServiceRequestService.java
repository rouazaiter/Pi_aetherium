package tn.esprit.backend.services.interfaces;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.backend.dto.CreateServiceRequestRequest;
import tn.esprit.backend.dto.ServiceRequestResponse;
import tn.esprit.backend.dto.UpdateServiceRequestRequest;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;

import java.util.List;

public interface ServiceRequestService {
    ServiceRequestResponse createServiceRequest(Long creatorId, CreateServiceRequestRequest request, MultipartFile file);
    ServiceRequestResponse getServiceRequestById(Long id, Long viewerId);
    List<ServiceRequestResponse> getAllServiceRequests(Long viewerId);
    List<ServiceRequestResponse> getServiceRequestsByStatus(Long viewerId, ServiceRequestStatus status);
    List<ServiceRequestResponse> getServiceRequestsByUser(Long userId);
    ServiceRequestResponse updateServiceRequest(Long id, Long requesterId, UpdateServiceRequestRequest request, MultipartFile file);
    void deleteServiceRequest(Long id, Long requesterId);
    int checkAndUpdateExpiredRequests();
}
