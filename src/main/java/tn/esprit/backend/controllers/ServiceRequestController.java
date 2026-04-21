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
     * Publier une nouvelle demande de service.
     */
    @PostMapping("/addservice/{creator-id}")
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(
            @PathVariable("creator-id") Long creatorId,
            @Valid @RequestBody CreateServiceRequestRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRequestService.createServiceRequest(creatorId, request, null));
    }

    /**
     * Variante "vrai upload" (multipart/form-data) pour joindre un fichier PDF/Word/PNG.
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
     * Recuperer toutes les demandes de service.
     */
    @GetMapping("/requests")
    public ResponseEntity<List<ServiceRequestResponse>> getAllServiceRequests(@RequestParam Long viewerId) {
        return ResponseEntity.ok(serviceRequestService.getAllServiceRequests(viewerId));
    }

    /**
     * Recuperer une demande de service par son id.
     */
    @GetMapping("/request/{id}")
    public ResponseEntity<ServiceRequestResponse> getServiceRequestById(@PathVariable Long id, @RequestParam Long viewerId) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestById(id, viewerId));
    }

    /**
     * Recuperer les demandes de service par statut.
     */
    @GetMapping("/requestbystatus/{status}")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByStatus(@PathVariable ServiceRequestStatus status, @RequestParam Long viewerId) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByStatus(viewerId, status));
    }

    /**
     * Recuperer les demandes creees par un utilisateur.
     */
    @GetMapping("/requestbyuser/{userId}")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByUser(userId));
    }

    /**
     * Mettre a jour une demande de service (createur uniquement).
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
     * Variante upload "multipart/form-data" pour modifier une demande (optionnellement avec un nouveau fichier).
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
     * Supprimer une demande de service (createur uniquement).
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
     * Executer manuellement la verification des demandes expirees.
     */
    @PostMapping("/expired")
    public ResponseEntity<Map<String, Integer>> checkExpiredRequests() {
        int updated = serviceRequestService.checkAndUpdateExpiredRequests();
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
