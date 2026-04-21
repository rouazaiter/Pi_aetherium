package tn.esprit.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.CreateServiceRequestRequest;
import tn.esprit.backend.dto.ServiceRequestResponse;
import tn.esprit.backend.dto.UpdateServiceRequestRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.services.interfaces.ServiceRequestService;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    /**
     * Publish a new service request.
     */
    @PostMapping("/addservice/{creator-id}")
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(
            @PathVariable("creator-id") Long creatorId,
            @Valid @RequestBody CreateServiceRequestRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRequestService.createServiceRequest(creatorId, request, null));
    }

    /**
     * Multipart upload variant for attaching a PDF/Word/PNG file.
     */
    @PostMapping(value = "/addservice/{creator-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceRequestResponse> createServiceRequestMultipart(
            @PathVariable("creator-id") Long creatorId,
            @Valid @RequestPart("payload") CreateServiceRequestRequest request,
            @RequestPart(required = false) MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceRequestService.createServiceRequest(creatorId, request, file));
    }

    /**
     * Retrieve all service requests.
     */
    @GetMapping("/requests")
    public ResponseEntity<List<ServiceRequestResponse>> getAllServiceRequests(@RequestParam Long viewerId) {
        return ResponseEntity.ok(serviceRequestService.getAllServiceRequests(viewerId));
    }

    /**
     * Retrieve a service request by id.
     */
    @GetMapping("/request/{id}")
    public ResponseEntity<ServiceRequestResponse> getServiceRequestById(@PathVariable Long id, @RequestParam Long viewerId) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestById(id, viewerId));
    }

    /**
     * Retrieve service requests by status.
     */
    @GetMapping("/requestbystatus/{status}")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByStatus(@PathVariable ServiceRequestStatus status, @RequestParam Long viewerId) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByStatus(viewerId, status));
    }

    /**
     * Retrieve the requests created by a user.
     */
    @GetMapping("/requestbyuser/{userId}")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByUser(userId));
    }

    /**
     * Update a service request (creator only).
     */
    @PutMapping("/modifyrequest/{id}/{requester-id}")
    public ResponseEntity<ServiceRequestResponse> updateServiceRequest(
            @PathVariable Long id,
            @PathVariable("requester-id") Long requesterId,
            @Valid @RequestBody UpdateServiceRequestRequest request
    ) {
        return ResponseEntity.ok(serviceRequestService.updateServiceRequest(id, requesterId, request, null));
    }

    /**
     * Multipart upload variant for updating a request with an optional new file.
     */
    @PutMapping(value = "/modifyrequest/{id}/{requester-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceRequestResponse> updateServiceRequestMultipart(
            @PathVariable Long id,
            @PathVariable("requester-id") Long requesterId,
            @Valid @RequestPart("payload") UpdateServiceRequestRequest request,
            @RequestPart(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(serviceRequestService.updateServiceRequest(id, requesterId, request, file));
    }

    /**
     * Delete a service request (creator only).
     */
    @DeleteMapping("/removerequest/{id}/{requester-id}")
    public ResponseEntity<Void> deleteServiceRequest(
            @PathVariable Long id,
            @PathVariable("requester-id") Long requesterId
    ) {
        serviceRequestService.deleteServiceRequest(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Manually trigger the expired requests check.
     */
    @PostMapping("/expired")
    public ResponseEntity<Map<String, Integer>> checkExpiredRequests() {
        int updated = serviceRequestService.checkAndUpdateExpiredRequests();
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
