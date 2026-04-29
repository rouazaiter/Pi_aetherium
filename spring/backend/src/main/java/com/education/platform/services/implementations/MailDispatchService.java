package com.education.platform.services.implementations;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.education.platform.repositories.user.UserRepository;

import java.util.List;

@Service
public class MailDispatchService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public MailDispatchService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendWarningEmail(Long userId, int count, List<String> detectedWords) {
        System.out.println("[MAIL-DEBUG] sendWarningEmail called for userId=" + userId + " on thread=" + Thread.currentThread().getName());

        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            System.err.println("[MAIL-DEBUG] User not found in DB for id=" + userId);
            return;
        }

        var user = userOpt.get();
        System.out.println("[MAIL-DEBUG] Found user: " + user.getUsername() + " email=" + user.getEmail());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("aichasmaoui22@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject("SkillHub — Community Guidelines Warning");
            message.setText(buildEmailBody(user.getUsername(), count, detectedWords));
            System.out.println("[MAIL-DEBUG] Calling mailSender.send() to " + user.getEmail());
            mailSender.send(message);
            System.out.println("[MAIL-DEBUG] mailSender.send() completed successfully");
        } catch (Exception e) {
            System.err.println("[MAIL-DEBUG] Exception during send: " + e.getClass().getName() + ": " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("[MAIL-DEBUG] Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                cause = cause.getCause();
            }
            e.printStackTrace();
        }
    }

    private String buildEmailBody(String username, int count, List<String> detectedWords) {
        return """
            Hello %s,

            Our moderation system has detected that you have attempted to post inappropriate content
            %d times on SkillHub. This is an automated warning.

            Detected inappropriate words in your last attempt: %s

            Please review our Community Guidelines:
            - Be respectful to other members
            - Avoid offensive, hateful, or inappropriate language
            - Constructive criticism is welcome, personal attacks are not

            If this behavior continues, your account may be temporarily restricted.

            This is an automated message — please do not reply.

            The SkillHub Team
            """.formatted(username, count, String.join(", ", detectedWords));
    }
}
