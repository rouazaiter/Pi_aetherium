package tn.esprit.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.backend.dto.CreateApplicationRequest;
import tn.esprit.backend.dto.CheckoutSessionResponse;
import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.ApplicationStatus;
import tn.esprit.backend.services.interfaces.ApplicationService;
import tn.esprit.backend.services.interfaces.PaymentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final PaymentService paymentService;

    /**
     * Postuler a une demande de service.
     */
    @PostMapping("/add-application/{applicant-id}/{service-request-id}")
    public ResponseEntity<Application> createApplication(
            @PathVariable("applicant-id") Long applicantId,
            @PathVariable("service-request-id") Long serviceRequestId,
            @Valid @RequestBody CreateApplicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(applicationService.createApplication(applicantId, serviceRequestId, request.message()));
    }

    /**
     * Recuperer une candidature par son id.
     */
    @GetMapping("/retrieve-application/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    /**
     * Recuperer les candidatures d'un utilisateur.
     */
    @GetMapping("/retrieve-by-user/{applicantId}")
    public ResponseEntity<List<Application>> getApplicationsByUser(@PathVariable Long applicantId) {
        return ResponseEntity.ok(applicationService.getApplicationsByUser(applicantId));
    }

    /**
     * Recuperer les candidatures d'une demande (createur uniquement).
     */
    @GetMapping("/retrieve-by-service-request/{serviceRequestId}/{requester-id}")
    public ResponseEntity<List<Application>> getApplicationsByServiceRequest(
            @PathVariable Long serviceRequestId,
            @PathVariable("requester-id") Long requesterId
    ) {
        return ResponseEntity.ok(applicationService.getApplicationsByServiceRequest(serviceRequestId, requesterId));
    }

    /**
     * Recuperer les candidatures d'une demande filtrees par statut (createur uniquement).
     */
    @GetMapping("/retrieve-by-service-request-status/{serviceRequestId}/{status}/{requester-id}")
    public ResponseEntity<List<Application>> getApplicationsByServiceRequestAndStatus(
            @PathVariable Long serviceRequestId,
            @PathVariable ApplicationStatus status,
            @PathVariable("requester-id") Long requesterId
    ) {
        return ResponseEntity.ok(applicationService.getApplicationsByServiceRequestAndStatus(serviceRequestId, status, requesterId));
    }

    /**
     * Changer le statut d'une candidature (createur de la demande uniquement).
     */
    @PatchMapping("/modify-status/{applicationId}/{requester-id}/{status}")
    public ResponseEntity<Application> updateApplicationStatus(
            @PathVariable Long applicationId,
            @PathVariable("requester-id") Long requesterId,
            @PathVariable ApplicationStatus status
    ) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(applicationId, requesterId, status));
    }

    /**
     * Verifier si un utilisateur a deja postule a une demande.
     */
    @GetMapping("/has-applied/{serviceRequestId}/{applicantId}")
    public ResponseEntity<Map<String, Boolean>> hasApplied(
            @PathVariable Long serviceRequestId,
            @PathVariable Long applicantId
    ) {
        boolean result = applicationService.hasUserApplied(serviceRequestId, applicantId);
        return ResponseEntity.ok(Map.of("hasApplied", result));
    }

    /**
     * Accepter une candidature et créer une session Stripe Checkout pour le paiement.
     */
    @PostMapping("/accept-and-checkout/{applicationId}/{requester-id}")
    public ResponseEntity<CheckoutSessionResponse> acceptAndCreateCheckout(
            @PathVariable Long applicationId,
            @PathVariable("requester-id") Long requesterId
    ) {
        // Accepter l'application
        applicationService.updateApplicationStatus(applicationId, requesterId, ApplicationStatus.ACCEPTED);
        
        // Créer la session Stripe Checkout
        return ResponseEntity.ok(paymentService.createCheckoutSessionForApplication(applicationId, requesterId));
    }
}
