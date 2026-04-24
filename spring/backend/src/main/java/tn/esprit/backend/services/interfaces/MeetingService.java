package tn.esprit.backend.services.interfaces;

import tn.esprit.backend.dto.MeetingConfigRequest;
import tn.esprit.backend.dto.MeetingConfigResponse;
import tn.esprit.backend.dto.MeetingReservationRequest;
import tn.esprit.backend.dto.MeetingReservationResponse;
import tn.esprit.backend.entities.MeetingStatus;

public interface MeetingService {
    MeetingConfigResponse upsertConfig(Long serviceRequestId, Long requesterId, MeetingConfigRequest request);
    MeetingConfigResponse getConfig(Long serviceRequestId);
    MeetingReservationResponse reserve(Long applicationId, Long applicantId, MeetingReservationRequest request);
    MeetingReservationResponse getByApplication(Long applicationId, Long requesterId);
    MeetingReservationResponse updateStatus(Long applicationId, Long requesterId, MeetingStatus status);
}
