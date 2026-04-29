package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String to, String code, String certTitle) {
        System.out.println("--------------------------------------------------");
        System.out.println("VERIFICATION CODE FOR: " + to);
        System.out.println("CODE: " + code);
        System.out.println("--------------------------------------------------");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "SkillHub Contact");
            helper.setTo(to);
            helper.setSubject("SkillHub: Your Verification Code for " + certTitle);
            
            String htmlContent = """
                <div style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #0f172a; margin: 0; font-size: 24px;">SkillHub</h1>
                        <p style="color: #64748b; margin: 5px 0 0;">Professional Certification Platform</p>
                    </div>
                    
                    <div style="background-color: #f8fafc; padding: 30px; border-radius: 8px; text-align: center;">
                        <p style="font-size: 16px; color: #1e293b; margin-top: 0;">Thank you for your purchase of:</p>
                        <h2 style="color: #6366f1; margin: 10px 0 25px; font-size: 20px;">%s</h2>
                        
                        <p style="font-size: 14px; color: #64748b; margin-bottom: 10px;">Use the code below to verify your enrollment:</p>
                        <div style="background: white; border: 2px solid #e2e8f0; display: inline-block; padding: 15px 40px; border-radius: 12px; font-size: 32px; font-weight: 800; letter-spacing: 5px; color: #0f172a; margin: 10px 0;">
                            %s
                        </div>
                        <p style="font-size: 12px; color: #94a3b8; margin-top: 20px;">This code is valid for 5 minutes only.</p>
                    </div>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #f1f5f9; text-align: center;">
                        <p style="font-size: 12px; color: #94a3b8; margin: 0;">
                            &copy; 2026 SkillHub. All rights reserved.<br>
                            If you did not request this code, please ignore this email.
                        </p>
                    </div>
                </div>
                """.formatted(certTitle, code);
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Professional HTML email sent to: " + to);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("CRITICAL: Failed to send email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR: SMTP Configuration error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
