package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SmsService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    /**
     * This must be your TWILIO-ASSIGNED phone number (from console.twilio.com → Active Numbers),
     * NOT your personal number. Your personal number is the "to" number entered by the user.
     * Example: +15017122661
     */
    @Value("${twilio.phone.number:}")
    private String fromNumber;

    private boolean configured = false;

    @PostConstruct
    public void init() {
        boolean hasRealSid   = accountSid  != null && accountSid.startsWith("AC")
                               && accountSid.length() == 34
                               && !accountSid.contains("x");
        boolean hasRealToken = authToken   != null && !authToken.isBlank()
                               && !authToken.equals("your_auth_token");
        boolean hasFrom      = fromNumber  != null && !fromNumber.isBlank()
                               && fromNumber.startsWith("+");

        configured = hasRealSid && hasRealToken && hasFrom;

        if (configured) {
            Twilio.init(accountSid, authToken);
            System.out.println("[SMS] ✅ Twilio initialised");
            System.out.println("[SMS]    Account SID : " + accountSid);
            System.out.println("[SMS]    From number : " + fromNumber);
            System.out.println("[SMS]    NOTE: 'from' must be your Twilio number, not your personal number.");
        } else {
            System.out.println("[SMS] ⚠️  Twilio not configured — SMS disabled.");
            System.out.println("[SMS]    hasRealSid=" + hasRealSid
                    + " hasRealToken=" + hasRealToken
                    + " hasFrom=" + hasFrom);
        }
    }

    /**
     * Sends a payment confirmation SMS.
     * Never throws — logs errors instead so the main flow is never interrupted.
     */
    public void sendPaymentConfirmation(String toPhone, String holderName,
                                        String certTitle, BigDecimal amount) {
        if (!configured) {
            System.out.println("[SMS] Skipped (not configured). Would send to: " + toPhone);
            return;
        }
        if (toPhone == null || toPhone.isBlank()) {
            System.out.println("[SMS] Skipped — no phone number provided.");
            return;
        }

        // Normalise: remove spaces
        String to = toPhone.replaceAll("\\s+", "");

        System.out.println("[SMS] Attempting to send to: " + to + " from: " + fromNumber);

        String body = buildMessage(holderName, certTitle, amount);

        try {
            Message msg = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    body
            ).create();

            System.out.println("[SMS] ✅ Sent! SID=" + msg.getSid()
                    + " | Status=" + msg.getStatus()
                    + " | To=" + msg.getTo()
                    + " | From=" + msg.getFrom());

        } catch (ApiException e) {
            System.err.println("[SMS] ❌ Twilio API error: code=" + e.getCode()
                    + " | message=" + e.getMessage()
                    + " | moreInfo=" + e.getMoreInfo());
            System.err.println("[SMS]    to=" + to + " | from=" + fromNumber);
            System.err.println("[SMS]    Common causes:");
            System.err.println("[SMS]    - Error 21608: 'from' number is not a Twilio number (you may have set your personal number as 'from')");
            System.err.println("[SMS]    - Error 21219: 'to' number not verified (trial accounts only)");
            System.err.println("[SMS]    - Error 21614: Cannot send to this country/number type");
        } catch (Exception e) {
            System.err.println("[SMS] ❌ Unexpected error: " + e.getMessage());
        }
    }

    private String buildMessage(String name, String certTitle, BigDecimal amount) {
        String firstName = (name != null && name.contains(" "))
                ? name.substring(0, name.indexOf(' '))
                : (name != null && !name.isBlank() ? name : "there");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return String.format(
                "Hi %s! Your enrollment in \"%s\" on SkillHub is confirmed (FREE). " +
                "Good luck on your exam! — SkillHub",
                firstName, certTitle
            );
        }

        return String.format(
            "Hi %s! Payment confirmed: $%s charged for \"%s\" on SkillHub. " +
            "Your certification is now unlocked. Good luck! — SkillHub",
            firstName,
            amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
            certTitle
        );
    }
}
