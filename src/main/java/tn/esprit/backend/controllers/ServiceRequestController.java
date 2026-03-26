package tn.esprit.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.services.interfaces.FileStorageService;
import tn.esprit.backend.services.interfaces.ServiceRequestService;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final FileStorageService fileStorageService;

    /**
     * Publier une nouvelle demande de service.
     */
    @PostMapping("/add-service-request/{creator-id}")
    public ResponseEntity<ServiceRequest> createServiceRequest(
            @PathVariable("creator-id") Long creatorId,
            @Valid @RequestBody ServiceRequest serviceRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRequestService.createServiceRequest(creatorId, serviceRequest));
    }

    /**
     * Variante "vrai upload" (multipart/form-data) pour joindre un fichier PDF/Word/PNG.
     */
    @PostMapping(value = "/add-service-request/{creator-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceRequest> createServiceRequestMultipart(
            @PathVariable("creator-id") Long creatorId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiringDate,
            @RequestPart(required = false) MultipartFile file
    ) {
        ServiceRequest sr = new ServiceRequest();
        sr.setName(name);
        sr.setDescription(description);
        sr.setExpiringDate(expiringDate);

        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.store(file);
            sr.setFiles("/api/service-requests/files/" + storedFileName);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceRequestService.createServiceRequest(creatorId, sr));
    }

    /**
     * Recuperer toutes les demandes de service.
     */
    @GetMapping("/retrieve-all-service-requests")
    public ResponseEntity<List<ServiceRequest>> getAllServiceRequests() {
        return ResponseEntity.ok(serviceRequestService.getAllServiceRequests());
    }

    /**
     * Recuperer une demande de service par son id.
     */
    @GetMapping("/retrieve-service-request/{id}")
    public ResponseEntity<ServiceRequest> getServiceRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestById(id));
    }

    /**
     * Recuperer les demandes de service par statut.
     */
    @GetMapping("/retrieve-by-status/{status}")
    public ResponseEntity<List<ServiceRequest>> getServiceRequestsByStatus(@PathVariable ServiceRequestStatus status) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByStatus(status));
    }

    /**
     * Recuperer les demandes creees par un utilisateur.
     */
    @GetMapping("/retrieve-by-user/{userId}")
    public ResponseEntity<List<ServiceRequest>> getServiceRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByUser(userId));
    }

    /**
     * Mettre a jour une demande de service (createur uniquement).
     */
    @PutMapping("/modify-service-request/{id}/{requester-id}")
    public ResponseEntity<ServiceRequest> updateServiceRequest(
            @PathVariable Long id,
            @PathVariable("requester-id") Long requesterId,
            @RequestBody ServiceRequest serviceRequest
    ) {
        return ResponseEntity.ok(serviceRequestService.updateServiceRequest(id, requesterId, serviceRequest));
    }

    /**
     * Variante upload "multipart/form-data" pour modifier une demande (optionnellement avec un nouveau fichier).
     */
    @PutMapping(value = "/modify-service-request/{id}/{requester-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceRequest> updateServiceRequestMultipart(
            @PathVariable Long id,
            @PathVariable("requester-id") Long requesterId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiringDate,
            @RequestPart(required = false) MultipartFile file
    ) {
        ServiceRequest payload = new ServiceRequest();

        if (name != null) {
            payload.setName(name);
        }
        if (description != null) {
            payload.setDescription(description);
        }
        if (expiringDate != null) {
            payload.setExpiringDate(expiringDate);
        }

        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.store(file);
            payload.setFiles("/api/service-requests/files/" + storedFileName);
        }

        return ResponseEntity.ok(serviceRequestService.updateServiceRequest(id, requesterId, payload));
    }

    /**
     * Supprimer une demande de service (createur uniquement).
     */
    @DeleteMapping("/remove-service-request/{id}/{requester-id}")
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
    @PostMapping("/check-expired")
    public ResponseEntity<Map<String, Integer>> checkExpiredRequests() {
        int updated = serviceRequestService.checkAndUpdateExpiredRequests();
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
