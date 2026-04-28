package com.education.plateform.dto;

import com.education.plateform.entities.ServiceRequestCategory;
import com.education.plateform.entities.ServiceRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DashboardEventProjection {
    Long getId();
    String getEventName();
    LocalDateTime getEventDate();
    ServiceRequestCategory getCategory();
    ServiceRequestStatus getStatus();
    BigDecimal getAmount();
    Long getParticipants();
}
