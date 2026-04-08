package com.education.platform.services.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

@Service
public class AccountMailService {

    private static final Logger log = LoggerFactory.getLogger(AccountMailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.mail.from:}")
    private String appMailFrom;

    @Value("${spring.mail.username:}")
    private String springMailUsername;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public AccountMailService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    void logMailStartupHint() {
        if (!StringUtils.hasText(mailHost)) {
            log.warn(
                    "E-mail désactivé : spring.mail.host est vide. Aucun message ne partira par SMTP ; le lien de "
                            + "réinitialisation sera seulement loggé (niveau INFO). Ajoutez host/user/password + "
                            + "app.mail.from dans application.properties pour Gmail.");
        } else if (!StringUtils.hasText(resolveFrom())) {
            log.warn(
                    "spring.mail.host est défini mais app.mail.from et spring.mail.username sont vides : "
                            + "l'envoi SMTP sera ignoré. Définissez app.mail.from=votre@gmail.com (Gmail).");
        } else {
            log.info("SMTP configuré (host={}) — les e-mails de réinitialisation seront envoyés.", mailHost);
        }
    }

    public void sendProfileAccessCode(String toEmail, String code) {
        String subject = "SkillHub — code de vérification";
        String body =
                "Bonjour,\n\n"
                        + "Pour accéder à votre profil SkillHub, utilisez ce code à 6 chiffres :\n\n"
                        + code
                        + "\n\n"
                        + "Il expire dans 10 minutes. Si vous n'avez pas demandé ce code, ignorez ce message.\n";

        JavaMailSender sender = mailSender.getIfAvailable();
        String from = resolveFrom();
        if (sender == null) {
            log.warn("Pas de JavaMailSender — e-mail non envoyé. Code profil pour {} : {}", toEmail, code);
            return;
        }
        if (!StringUtils.hasText(from)) {
            log.warn("Expéditeur vide — e-mail non envoyé. Code profil pour {} : {}", toEmail, code);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
            log.info("Code d'accès profil envoyé à {}.", toEmail);
        } catch (Exception e) {
            log.error("Échec envoi code profil vers {} — code de secours ci-dessous.", toEmail, e);
            log.info("Code profil pour {} : {}", toEmail, code);
        }
    }

    public void sendPasswordReset(String toEmail, String resetUrl) {
        String subject = "Réinitialisation du mot de passe";
        String body =
                "Bonjour,\n\n"
                        + "Pour choisir un nouveau mot de passe, ouvrez ce lien dans votre navigateur :\n"
                        + resetUrl
                        + "\n\n"
                        + "Ce lien expire dans une heure. Si vous n'avez pas demandé cette réinitialisation, ignorez ce message.\n";

        JavaMailSender sender = mailSender.getIfAvailable();
        String from = resolveFrom();
        if (sender == null) {
            log.warn(
                    "Pas de JavaMailSender (spring.mail.* incomplet ?). E-mail non envoyé. Lien pour {} ci-dessous.",
                    toEmail);
            log.info("Réinitialisation mot de passe pour {} — lien : {}", toEmail, resetUrl);
            return;
        }
        if (!StringUtils.hasText(from)) {
            log.warn("app.mail.from / spring.mail.username vide — e-mail non envoyé. Lien pour {} ci-dessous.", toEmail);
            log.info("Réinitialisation mot de passe pour {} — lien : {}", toEmail, resetUrl);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
            log.info("E-mail de réinitialisation envoyé avec succès à {} (expéditeur {}).", toEmail, from);
        } catch (Exception e) {
            log.error(
                    "Échec SMTP vers {} (vérifiez mot de passe d'application Gmail, pas le mot de passe du compte). "
                            + "Lien de secours ci-dessous.",
                    toEmail,
                    e);
            log.info("Réinitialisation mot de passe pour {} — lien : {}", toEmail, resetUrl);
        }
    }

    private String resolveFrom() {
        if (StringUtils.hasText(appMailFrom)) {
            return appMailFrom.trim();
        }
        if (StringUtils.hasText(springMailUsername)) {
            return springMailUsername.trim();
        }
        return "";
    }
}
