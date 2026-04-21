package tn.esprit.backend.services.implementations;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.CheckoutSessionResponse;
import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.PaymentStatus;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.repositories.ApplicationRepository;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.services.interfaces.PaymentService;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.currency:usd}")
    private String stripeCurrency;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(Long serviceRequestId, Long requesterId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRequest not found: " + serviceRequestId));

        if (serviceRequest.getCreator() == null || !serviceRequest.getCreator().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can pay for this service request");
        }

        if (serviceRequest.getPrice() == null || serviceRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service request price is required");
        }

        Stripe.apiKey = stripeSecretKey;

        long amountInMinorUnits = serviceRequest.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendBaseUrl + "/marketplace?payment=success&requestId=" + serviceRequest.getId())
                .setCancelUrl(frontendBaseUrl + "/marketplace?payment=cancel&requestId=" + serviceRequest.getId())
                .putMetadata("serviceRequestId", String.valueOf(serviceRequest.getId()))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(stripeCurrency)
                                                .setUnitAmount(amountInMinorUnits)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Service Request - " + serviceRequest.getName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            Session session = Session.create(params);
            serviceRequestRepository.save(serviceRequest);
            return new CheckoutSessionResponse(session.getId(), session.getUrl());
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to create Stripe checkout session", ex);
        }
    }

    @Override
    @Transactional
    public void markAsPaidFromWebhook(Long serviceRequestId, Session session) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRequest not found: " + serviceRequestId));

        // Paiement du ServiceRequest n'existe plus, cette méthode est maintenant obsolète
        // mais on la garde pour compatibilité si besoin
    }

    @Override
    @Transactional
    public CheckoutSessionResponse createCheckoutSessionForApplication(Long applicationId, Long requesterId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + applicationId));

        ServiceRequest serviceRequest = application.getServiceRequest();

        if (!serviceRequest.getCreator().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the service request creator can accept this application");
        }

        if (application.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This application is already paid");
        }

        if (serviceRequest.getPrice() == null || serviceRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service request price is required");
        }

        Stripe.apiKey = stripeSecretKey;

        long amountInMinorUnits = serviceRequest.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendBaseUrl + "/applications?payment=success&applicationId=" + application.getId())
                .setCancelUrl(frontendBaseUrl + "/applications?payment=cancel&applicationId=" + application.getId())
                .putMetadata("applicationId", String.valueOf(application.getId()))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(stripeCurrency)
                                                .setUnitAmount(amountInMinorUnits)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Payment for Application - " + serviceRequest.getName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            Session session = Session.create(params);
            application.setStripeCheckoutSessionId(session.getId());
            applicationRepository.save(application);
            return new CheckoutSessionResponse(session.getId(), session.getUrl());
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to create Stripe checkout session", ex);
        }
    }

    @Override
    @Transactional
    public void markApplicationAsPaidFromWebhook(Long applicationId, Session session) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + applicationId));

        if (application.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        application.setPaymentStatus(PaymentStatus.PAID);
        application.setStripeCheckoutSessionId(session.getId());
        applicationRepository.save(application);

        // Notifier l'applicant que le paiement a été reçu
        notificationService.notifyUsersWithAssistant(
                java.util.List.of(application.getApplicant().getId()),
                "PAYMENT_RECEIVED",
                "Payment received for your application on: " + application.getServiceRequest().getName(),
                application.getServiceRequest().getCreator().getUsername(),
                application.getServiceRequest().getName(),
                tn.esprit.backend.dto.NotificationPriority.HIGH,
                "View application",
                application.getId(),
                null
        );
    }
}