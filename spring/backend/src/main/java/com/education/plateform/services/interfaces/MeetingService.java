package com.education.plateform.services.interfaces;

import com.education.plateform.dto.CalendlyEventDetailsResponse;
import com.education.plateform.dto.MeetingConfigRequest;
import com.education.plateform.dto.MeetingConfigResponse;
import com.education.plateform.dto.MeetingReservationRequest;
import com.education.plateform.dto.MeetingReservationResponse;
import com.education.plateform.entities.MeetingStatus;

public interface MeetingService {
    MeetingConfigResponse upsertConfig(Long serviceRequestId, Long requesterId, MeetingConfigRequest request);
    MeetingConfigResponse getConfig(Long serviceRequestId);
    MeetingReservationResponse reserve(Long applicationId, Long applicantId, MeetingReservationRequest request);
    MeetingReservationResponse getByApplication(Long applicationId, Long requesterId);
    MeetingReservationResponse updateStatus(Long applicationId, Long requesterId, MeetingStatus status);
    CalendlyEventDetailsResponse getCalendlyEvent(String eventUrl);
}
