package tn.esprit.backend.services.interfaces;

import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;

import java.util.List;

public interface ServiceRequestService {
    ServiceRequest createServiceRequest(Long creatorId, ServiceRequest serviceRequest);
    ServiceRequest getServiceRequestById(Long id);
    List<ServiceRequest> getAllServiceRequests();
    List<ServiceRequest> getServiceRequestsByStatus(ServiceRequestStatus status);
    List<ServiceRequest> getServiceRequestsByUser(Long userId);
    ServiceRequest updateServiceRequest(Long id, Long requesterId, ServiceRequest serviceRequest);
    void deleteServiceRequest(Long id, Long requesterId);
    int checkAndUpdateExpiredRequests();
}
