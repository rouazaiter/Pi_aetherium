package com.education.plateform.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.education.plateform.dto.CalendlyEventDetailsResponse;
import com.education.plateform.dto.MeetingConfigRequest;
import com.education.plateform.dto.MeetingConfigResponse;
import com.education.plateform.dto.MeetingReservationRequest;
import com.education.plateform.dto.MeetingReservationResponse;
import com.education.plateform.entities.MeetingStatus;
import com.education.plateform.services.interfaces.MeetingService;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
 //calendly 
    @PutMapping("/config/{serviceRequestId}/{requester-id}")
    public ResponseEntity<MeetingConfigResponse> upsertConfig(
            @PathVariable Long serviceRequestId,
            @PathVariable("requester-id") Long requesterId,
            @Valid @RequestBody MeetingConfigRequest request
    ) {
        return ResponseEntity.ok(meetingService.upsertConfig(serviceRequestId, requesterId, request));
    }

    @GetMapping("/config/{serviceRequestId}")
    public ResponseEntity<MeetingConfigResponse> getConfig(@PathVariable Long serviceRequestId) {
        return ResponseEntity.ok(meetingService.getConfig(serviceRequestId));
    }

    @PostMapping("/reserve/{applicationId}/{applicant-id}")
    public ResponseEntity<MeetingReservationResponse> reserve(
            @PathVariable Long applicationId,
            @PathVariable("applicant-id") Long applicantId,
            @Valid @RequestBody MeetingReservationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingService.reserve(applicationId, applicantId, request));
    }

    @GetMapping("/by-application/{applicationId}/{requester-id}")
    public ResponseEntity<MeetingReservationResponse> getByApplication(
            @PathVariable Long applicationId,
            @PathVariable("requester-id") Long requesterId
    ) {
        return ResponseEntity.ok(meetingService.getByApplication(applicationId, requesterId));
    }

    @GetMapping("/calendly-event")
    public ResponseEntity<CalendlyEventDetailsResponse> getCalendlyEvent(@RequestParam String eventUrl) {
        return ResponseEntity.ok(meetingService.getCalendlyEvent(eventUrl));
    }

    @PatchMapping("/status/{applicationId}/{requester-id}/{status}")
    public ResponseEntity<MeetingReservationResponse> updateStatus(
            @PathVariable Long applicationId,
            @PathVariable("requester-id") Long requesterId,
            @PathVariable MeetingStatus status
    ) {
        return ResponseEntity.ok(meetingService.updateStatus(applicationId, requesterId, status));
    }
}
