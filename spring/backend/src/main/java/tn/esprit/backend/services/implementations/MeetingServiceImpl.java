package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.CalendlyEventDetailsResponse;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class MeetingServiceImpl implements MeetingService {

    private static final String CALENDLY_API_BASE_URL = "https://api.calendly.com";

    private static final List<DateTimeFormatter> SLOT_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    );

    private final MeetingConfigRepository meetingConfigRepository;
    private final MeetingReservationRepository meetingReservationRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ApplicationRepository applicationRepository;

    @Value("${calendly.api-key:}")
    private String calendlyApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public MeetingConfigResponse upsertConfig(Long serviceRequestId, Long requesterId, MeetingConfigRequest request) {
        ServiceRequest serviceRequest = fetchServiceRequest(serviceRequestId);
        ensureCreator(serviceRequest, requesterId);

        String calendlyLink = normalize(request.calendlyLink());
        Integer durationMinutes = request.durationMinutes();
        List<String> availableSlots = normalizeSlots(request.availableSlots());

        if (calendlyLink == null || calendlyLink.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Calendly link is required");
        }
        if (durationMinutes == null || durationMinutes < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session duration must be at least 1 minute");
        }
        if (!availableSlots.isEmpty()) {
            ensureExpiringDateAfterAllSlots(serviceRequest, availableSlots);
        }

        MeetingConfig config = meetingConfigRepository.findByServiceRequest(serviceRequest)
                .orElseGet(() -> MeetingConfig.builder().serviceRequest(serviceRequest).build());

        config.setCalendlyLink(calendlyLink);
        config.setDurationMinutes(durationMinutes);
        config.setAvailableSlotsText(serializeSlots(availableSlots));
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
     // reserve
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

        String creatorCalendlyLink = normalize(config.getCalendlyLink());
        if (creatorCalendlyLink == null || creatorCalendlyLink.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator Calendly URL is required for scheduling this slot");
        }

        String candidateCalendlyUrl = normalize(request.candidateCalendlyUrl());
        if (candidateCalendlyUrl == null || candidateCalendlyUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate Calendly URL is required");
        }

        MeetingReservation reservation = meetingReservationRepository.findByApplication(application)
                .orElseGet(() -> MeetingReservation.builder()
                        .application(application)
                        .serviceRequest(serviceRequest)
                        .applicant(application.getApplicant())
                        .build());

        reservation.setSource(source);
        reservation.setSlot(slot);
        reservation.setCandidateCalendlyUrl(candidateCalendlyUrl);
        reservation.setCalendlyEventUrl(createCalendlySchedulingLink(config.getCalendlyLink(), slot, application.getApplicant().getUsername(), application.getApplicant().getEmail(), config.getDurationMinutes()));
        reservation.setStatus(MeetingStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setConfirmedAt(null);

        MeetingReservation saved = meetingReservationRepository.save(reservation);
        return toReservationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CalendlyEventDetailsResponse getCalendlyEvent(String eventUrl) {
        if (eventUrl == null || eventUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Calendly event URL is required");
        }

        if (calendlyApiKey == null || calendlyApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendly API key is not configured");
        }

        String eventId = extractCalendlyEventId(eventUrl);
        if (eventId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to parse Calendly event ID from URL");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(calendlyApiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String apiUrl = CALENDLY_API_BASE_URL + "/scheduled_events/" + eventId;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to retrieve Calendly event details");
            }

            Map<?, ?> body = response.getBody();
            Map<?, ?> resource = (Map<?, ?>) body.get("resource");
            if (resource == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected Calendly response structure");
            }

            return new CalendlyEventDetailsResponse(
                    (String) resource.get("uri"),
                    (String) resource.get("name"),
                    (String) resource.get("start_time"),
                    (String) resource.get("end_time"),
                    (String) resource.get("status"),
                    resource.get("location") != null ? resource.get("location").toString() : null,
                    (String) resource.get("invitee_url")
            );
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Calendly API error: " + ex.getStatusCode(), ex);
        }
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

    private MeetingSource parseSource(String source) {
        if (source == null || source.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meeting source is required");
        }
        try {
            return MeetingSource.valueOf(source.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid meeting source. Expected one of: " + Arrays.toString(MeetingSource.values()));
        }
    }

    private String createCalendlySchedulingLink(String calendlyLink, String slot, String inviteeName, String inviteeEmail, Integer durationMinutes) {
        try {
            String hostUri = getCalendlyUserUri();
            Integer duration = durationMinutes != null ? durationMinutes : 30;
            String oneOffLink = createCalendlyOneOffEventType(hostUri, inviteeName, duration, slot);
            if (oneOffLink != null) {
                return oneOffLink;
            }
        } catch (Exception ignored) {
            // fallback to a simple pre-filled Calendly link when API creation fails
        }
        return buildCalendlySchedulingLink(calendlyLink, slot, inviteeName, inviteeEmail);
    }

    private String createCalendlyOneOffEventType(String hostUri, String name, int durationMinutes, String slot) {
        LocalDateTime slotDateTime = parseSlotDateTime(slot);
        String date = slotDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Map<String, Object> dateSetting = Map.of(
                "type", "date_range",
                "start_date", date,
                "end_date", date
        );
        Map<String, Object> location = Map.of(
                "kind", "physical",
                "location", "Calendly"
        );
        Map<String, Object> payload = Map.of(
                "name", name != null && !name.isBlank() ? name : "Meeting",
                "host", hostUri,
                "duration", durationMinutes,
                "timezone", "UTC",
                "date_setting", dateSetting,
                "location", location
        );

        HttpHeaders headers = createCalendlyHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String apiUrl = CALENDLY_API_BASE_URL + "/one_off_event_types";

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
        if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        Map<?, ?> body = response.getBody();
        if (body == null) {
            return null;
        }
        Map<?, ?> resource = (Map<?, ?>) body.get("resource");
        if (resource == null) {
            return null;
        }
        if (resource.get("scheduling_url") instanceof String) {
            return (String) resource.get("scheduling_url");
        }
        if (resource.get("uri") instanceof String) {
            return (String) resource.get("uri");
        }
        return null;
    }

    private String getCalendlyUserUri() {
        HttpHeaders headers = createCalendlyHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String apiUrl = CALENDLY_API_BASE_URL + "/users/me";
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to retrieve Calendly user information");
        }
        Map<?, ?> resource = (Map<?, ?>) response.getBody().get("resource");
        if (resource == null || !(resource.get("uri") instanceof String)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected Calendly user response structure");
        }
        return (String) resource.get("uri");
    }

    private HttpHeaders createCalendlyHeaders() {
        if (calendlyApiKey == null || calendlyApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendly API key is not configured");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(calendlyApiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String buildCalendlySchedulingLink(String calendlyLink, String slot, String inviteeName, String inviteeEmail) {
        String normalizedLink = normalize(calendlyLink);
        if (normalizedLink == null || normalizedLink.isBlank()) {
            return null;
        }

        LocalDateTime slotDateTime = parseSlotDateTime(slot);
        ZonedDateTime utcSlot = slotDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        String formattedDate = utcSlot.format(DateTimeFormatter.ISO_INSTANT);

        StringBuilder builder = new StringBuilder(normalizedLink);
        builder.append(normalizedLink.contains("?") ? '&' : '?');
        builder.append("date=").append(URLEncoder.encode(formattedDate, StandardCharsets.UTF_8));

        if (inviteeName != null && !inviteeName.isBlank()) {
            builder.append("&name=").append(URLEncoder.encode(inviteeName, StandardCharsets.UTF_8));
        }
        if (inviteeEmail != null && !inviteeEmail.isBlank()) {
            builder.append("&email=").append(URLEncoder.encode(inviteeEmail, StandardCharsets.UTF_8));
        }

        return builder.toString();
    }

    private String extractCalendlyEventId(String eventUrl) {
        try {
            URI uri = new URI(eventUrl.trim());
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return null;
            }
            String[] segments = path.split("/");
            for (int i = segments.length - 1; i >= 0; i--) {
                if (segments[i] != null && !segments[i].isBlank()) {
                    return segments[i];
                }
            }
        } catch (URISyntaxException ignored) {
            // fall through to return null
        }
        return null;
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
                config.getDurationMinutes(),
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
                reservation.getCandidateCalendlyUrl(),
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

    private void ensureExpiringDateAfterAllSlots(ServiceRequest serviceRequest, List<String> slots) {
        LocalDateTime expiringDate = serviceRequest.getExpiringDate();
        if (expiringDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Service request expiration date is required before configuring meeting slots");
        }

        for (String rawSlot : slots) {
            LocalDateTime slotDateTime = parseSlotDateTime(rawSlot);
            if (!expiringDate.isAfter(slotDateTime)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Expiration date must be greater than every available slot");
            }
        }
    }

    private LocalDateTime parseSlotDateTime(String rawSlot) {
        String slot = normalize(rawSlot);
        if (slot == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slot date format");
        }

        for (DateTimeFormatter formatter : SLOT_FORMATTERS) {
            try {
                return LocalDateTime.parse(slot, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next formatter.
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid slot date format. Expected ISO date-time, e.g. 2026-04-20T14:30");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
