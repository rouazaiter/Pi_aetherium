package com.education.plateform.dto;

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
