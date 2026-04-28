package com.education.plateform.services.interfaces;

import org.springframework.web.multipart.MultipartFile;
import com.education.plateform.dto.CreateServiceRequestRequest;
import com.education.plateform.dto.ServiceRequestResponse;
import com.education.plateform.dto.UpdateServiceRequestRequest;
import com.education.plateform.entities.ServiceRequestStatus;

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
