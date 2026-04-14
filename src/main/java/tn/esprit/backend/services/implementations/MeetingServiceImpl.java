package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.MeetingConfigRequest;
import tn.esprit.backend.dto.MeetingConfigResponse;
import tn.esprit.backend.dto.MeetingReservationRequest;
import tn.esprit.backend.dto.MeetingReservationResponse;
import tn.esprit.backend.entities.*;
import tn.esprit.backend.repositories.ApplicationRepository;
import tn.esprit.backend.repositories.MeetingConfigRepository;
import tn.esprit.backend.repositories.MeetingReservationRepository;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.services.interfaces.MeetingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingConfigRepository meetingConfigRepository;
    private final MeetingReservationRepository meetingReservationRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public MeetingConfigResponse upsertConfig(Long serviceRequestId, Long requesterId, MeetingConfigRequest request) {
        ServiceRequest serviceRequest = fetchServiceRequest(serviceRequestId);
        ensureCreator(serviceRequest, requesterId);

        String calendlyLink = normalize(request.calendlyLink());
        List<String> normalizedSlots = normalizeSlots(request.availableSlots());

        if (calendlyLink == null || calendlyLink.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Calendly link is required");
        }
        if (normalizedSlots.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one available meeting slot is required");
        }

        MeetingConfig config = meetingConfigRepository.findByServiceRequest(serviceRequest)
                .orElseGet(() -> MeetingConfig.builder().serviceRequest(serviceRequest).build());

        config.setCalendlyLink(calendlyLink);
        config.setAvailableSlotsText(serializeSlots(normalizedSlots));
        config.setUpdatedAt(LocalDateTime.now());

        MeetingConfig saved = meetingConfigRepository.save(config);
        return toConfigResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingConfigResponse getConfig(Long serviceRequestId) {
        ServiceRequest serviceRequest = fetchServiceRequest(serviceRequestId);
        MeetingConfig config = meetingConfigRepository.findByServiceRequest(serviceRequest)
                .orElseGet(() -> MeetingConfig.builder()
                        .serviceRequest(serviceRequest)
                        .calendlyLink(null)
                        .availableSlotsText("")
                        .updatedAt(serviceRequest.getUpdatedAt() != null ? serviceRequest.getUpdatedAt() : serviceRequest.getCreatedAt())
                        .build());
        return toConfigResponse(config);
    }

    @Override
    @Transactional
    public MeetingReservationResponse reserve(Long applicationId, Long applicantId, MeetingReservationRequest request) {
        Application application = fetchApplication(applicationId);

        if (!application.getApplicant().getId().equals(applicantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only applicant can reserve this meeting");
        }

        ServiceRequest serviceRequest = application.getServiceRequest();
        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meeting reservation is allowed only for OPEN requests");
        }

        MeetingSource source = parseSource(request.source());
        String slot = normalize(request.slot());
        if (slot == null || slot.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meeting slot is required");
        }

        MeetingConfig config = meetingConfigRepository.findByServiceRequest(serviceRequest)
                .orElseGet(() -> MeetingConfig.builder().serviceRequest(serviceRequest).build());

        validateAgainstConfig(source, slot, config);

        MeetingReservation reservation = meetingReservationRepository.findByApplication(application)
                .orElseGet(() -> MeetingReservation.builder()
                        .application(application)
                        .serviceRequest(serviceRequest)
                        .applicant(application.getApplicant())
                        .build());

        reservation.setSource(source);
        reservation.setSlot(slot);
        reservation.setCalendlyEventUrl(normalize(request.calendlyEventUrl()));
        reservation.setStatus(MeetingStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setConfirmedAt(null);

        MeetingReservation saved = meetingReservationRepository.save(reservation);
        return toReservationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingReservationResponse getByApplication(Long applicationId, Long requesterId) {
        Application application = fetchApplication(applicationId);
        ServiceRequest serviceRequest = application.getServiceRequest();

        boolean isApplicant = application.getApplicant().getId().equals(requesterId);
        boolean isCreator = serviceRequest.getCreator().getId().equals(requesterId);

        if (!isApplicant && !isCreator) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only applicant or request creator can access this meeting");
        }

        MeetingReservation reservation = meetingReservationRepository.findByApplication(application)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting reservation not found for application: " + applicationId));

        return toReservationResponse(reservation);
    }

    @Override
    @Transactional
    public MeetingReservationResponse updateStatus(Long applicationId, Long requesterId, MeetingStatus status) {
        if (status == MeetingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status PENDING is not allowed in update");
        }

        Application application = fetchApplication(applicationId);
        ServiceRequest serviceRequest = application.getServiceRequest();
        ensureCreator(serviceRequest, requesterId);

        MeetingReservation reservation = meetingReservationRepository.findByApplication(application)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting reservation not found for application: " + applicationId));

        reservation.setStatus(status);
        reservation.setConfirmedAt(LocalDateTime.now());

        MeetingReservation saved = meetingReservationRepository.save(reservation);
        return toReservationResponse(saved);
    }

    private void validateAgainstConfig(MeetingSource source, String slot, MeetingConfig config) {
        List<String> availableSlots = parseSlots(config.getAvailableSlotsText());

        if (source == MeetingSource.SLOTS) {
            if (availableSlots.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No available slots configured for this request");
            }

            boolean found = availableSlots.stream().anyMatch(s -> s.equalsIgnoreCase(slot));
            if (!found) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected slot is not in available slots");
            }
            return;
        }

        String calendlyLink = normalize(config.getCalendlyLink());
        if (calendlyLink == null || calendlyLink.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Calendly is not configured for this request");
        }
    }

    private MeetingSource parseSource(String rawSource) {
        if (rawSource == null || rawSource.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meeting source is required");
        }

        try {
            return MeetingSource.valueOf(rawSource.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported meeting source: " + rawSource);
        }
    }

    private ServiceRequest fetchServiceRequest(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRequest not found: " + id));
    }

    private Application fetchApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + id));
    }

    private void ensureCreator(ServiceRequest serviceRequest, Long requesterId) {
        if (!serviceRequest.getCreator().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only service request creator can perform this action");
        }
    }

    private MeetingConfigResponse toConfigResponse(MeetingConfig config) {
        return new MeetingConfigResponse(
                config.getServiceRequest().getId(),
                config.getCalendlyLink(),
                parseSlots(config.getAvailableSlotsText()),
                config.getUpdatedAt()
        );
    }

    private MeetingReservationResponse toReservationResponse(MeetingReservation reservation) {
        return new MeetingReservationResponse(
                reservation.getId(),
                reservation.getApplication().getId(),
                reservation.getServiceRequest().getId(),
                reservation.getApplicant().getId(),
                reservation.getApplicant().getUsername(),
                reservation.getSource().name(),
                reservation.getSlot(),
                reservation.getCalendlyEventUrl(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                reservation.getConfirmedAt()
        );
    }

    private String serializeSlots(List<String> slots) {
        List<String> normalized = normalizeSlots(slots);
        if (normalized.isEmpty()) {
            return "";
        }
        return String.join("\n", normalized);
    }

    private List<String> parseSlots(String slotsText) {
        if (slotsText == null || slotsText.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(slotsText.split("\\r?\\n"))
                .map(this::normalize)
                .filter(item -> item != null && !item.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> normalizeSlots(List<String> slots) {
        if (slots == null) {
            return new ArrayList<>();
        }

        return slots.stream()
                .map(this::normalize)
                .filter(item -> item != null && !item.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
