package tn.esprit.backend.dto;

import tn.esprit.backend.entities.ServiceRequestCategory;
import tn.esprit.backend.entities.ServiceRequestStatus;

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
