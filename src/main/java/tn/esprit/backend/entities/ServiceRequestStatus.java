package tn.esprit.backend.entities;

public enum ServiceRequestStatus {
    OPEN,      // Published request, accepts applications
    CLOSED,    // Request finished after an application is accepted
    EXPIRED    // Expiration date has passed
}
