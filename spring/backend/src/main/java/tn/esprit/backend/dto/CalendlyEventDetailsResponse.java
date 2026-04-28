package tn.esprit.backend.dto;

public record CalendlyEventDetailsResponse(
        String uri,
        String name,
        String startTime,
        String endTime,
        String status,
        String location,
        String inviteeUrl
) {
}
