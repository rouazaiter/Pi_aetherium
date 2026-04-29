package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.itextpdf.html2pdf.HtmlConverter;
import com.education.platform.entities.certifications.Enrollment;
import com.education.platform.repositories.certifications.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CertificateGenerationService {

    private final EnrollmentRepository enrollmentRepository;

    public byte[] generateCertificate(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (enrollment.getPassed() == null || !enrollment.getPassed()) {
            throw new RuntimeException("User did not pass the certification");
        }

        String htmlContent = buildHtmlContent(enrollment);
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, target);
        return target.toByteArray();
    }

    // ── HTML / CSS ────────────────────────────────────────────────────────────

    private String buildHtmlContent(Enrollment enrollment) {
        // Resolve display name
        String rawName = enrollment.getFullName();
        if (rawName == null || rawName.isBlank()) {
            String email = enrollment.getUserIdentifier();
            rawName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            String[] parts = rawName.replace('.', ' ').replace('_', ' ').split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty())
                    sb.append(Character.toUpperCase(p.charAt(0)))
                      .append(p.substring(1).toLowerCase()).append(" ");
            }
            rawName = sb.toString().trim();
        }

        String recipientName = rawName;
        String certTitle     = enrollment.getCertification().getTitle();
        String certCategory  = enrollment.getCertification().getCategory() != null
                ? enrollment.getCertification().getCategory().toUpperCase() : "PROFESSIONAL CERTIFICATION";
        String date   = enrollment.getCompletedAt()
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
        String certId = "SKH-" + String.format("%06d", enrollment.getId());

        String signatureSvg = buildSignatureSvg();

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/>"
            + "<style>" + buildCss() + "</style></head><body>"
            + "<div class=\"page\">"

            + "<div class=\"left-bar\"></div>"
            + "<div class=\"top-band\"><div class=\"top-band-inner\"></div></div>"
            + "<div class=\"bottom-band\"><div class=\"bottom-band-inner\"></div></div>"

            + "<div class=\"corner-ornament corner-tl\"><svg width=\"60\" height=\"60\" viewBox=\"0 0 60 60\"><path d=\"M0,0 L60,0 L60,8 L8,8 L8,60 L0,60 Z\" fill=\"#C9A84C\"/><path d=\"M0,0 L50,0 L50,4 L4,4 L4,50 L0,50 Z\" fill=\"#F0D060\"/></svg></div>"
            + "<div class=\"corner-ornament corner-tr\"><svg width=\"60\" height=\"60\" viewBox=\"0 0 60 60\"><path d=\"M60,0 L0,0 L0,8 L52,8 L52,60 L60,60 Z\" fill=\"#C9A84C\"/><path d=\"M60,0 L10,0 L10,4 L56,4 L56,50 L60,50 Z\" fill=\"#F0D060\"/></svg></div>"
            + "<div class=\"corner-ornament corner-bl\"><svg width=\"60\" height=\"60\" viewBox=\"0 0 60 60\"><path d=\"M0,60 L60,60 L60,52 L8,52 L8,0 L0,0 Z\" fill=\"#C9A84C\"/><path d=\"M0,60 L50,60 L50,56 L4,56 L4,10 L0,10 Z\" fill=\"#F0D060\"/></svg></div>"
            + "<div class=\"corner-ornament corner-br\"><svg width=\"60\" height=\"60\" viewBox=\"0 0 60 60\"><path d=\"M60,60 L0,60 L0,52 L52,52 L52,0 L60,0 Z\" fill=\"#C9A84C\"/><path d=\"M60,60 L10,60 L10,56 L56,56 L56,10 L60,10 Z\" fill=\"#F0D060\"/></svg></div>"

            + "<div class=\"watermark\">SKILLHUB</div>"

            + "<div class=\"content\">"
            + "  <div class=\"brand-row\">"
            + "    <div class=\"brand-logo\">S</div>"
            + "    <div class=\"brand-name\">SKILLHUB</div>"
            + "  </div>"
            + "  <div class=\"gold-line\"></div>"
            + "  <div class=\"cert-label\">THIS IS TO CERTIFY THAT</div>"
            + "  <div class=\"cert-title\">Certificate</div>"
            + "  <div class=\"cert-subtitle\">OF ACHIEVEMENT</div>"
            + "  <div class=\"thin-line\"></div>"
            + "  <div class=\"presented-to\">IS PROUDLY PRESENTED TO</div>"
            + "  <div class=\"recipient\">" + recipientName + "</div>"
            + "  <div class=\"completion-text\">FOR SUCCESSFULLY COMPLETING</div>"
            + "  <div class=\"exam-title\">" + certTitle + "</div>"
            + "  <div class=\"exam-category\">" + certCategory + "</div>"

            // ── Footer: date | signature | cert-id (no QR)
            + "  <div class=\"footer-row\">"

            + "    <div class=\"footer-col footer-col--left\">"
            + "      <div class=\"footer-label\">DATE OF COMPLETION</div>"
            + "      <div class=\"footer-value\">" + date + "</div>"
            + "    </div>"

            + "    <div class=\"footer-col footer-col--center\">"
            + "      <div class=\"sig-wrap\">" + signatureSvg + "</div>"
            + "      <div class=\"sig-line-rule\"></div>"
            + "      <div class=\"sig-name\">Kahia Ghassen</div>"
            + "      <div class=\"sig-title\">CHIEF EXECUTIVE OFFICER · SKILLHUB</div>"
            + "    </div>"

            + "    <div class=\"footer-col footer-col--right\">"
            + "      <div class=\"footer-label\">CERTIFICATE ID</div>"
            + "      <div class=\"cert-id\">" + certId + "</div>"
            + "    </div>"

            + "  </div>"
            + "</div>"
            + "</div>"
            + "</body></html>";
    }

    // ── Signature SVG ─────────────────────────────────────────────────────────

    private String buildSignatureSvg() {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"240\" height=\"64\" viewBox=\"0 0 240 64\">"
            + "<path d=\"M 10,56 Q 120,62 230,54\" fill=\"none\" stroke=\"#0d1b2a\" stroke-width=\"1.2\" stroke-linecap=\"round\" opacity=\"0.5\"/>"
            + "<text x=\"12\" y=\"38\" font-family=\"Georgia,'Times New Roman',serif\" font-size=\"28\" font-style=\"italic\" font-weight=\"400\" fill=\"#0d1b2a\" letter-spacing=\"1\">Kahia</text>"
            + "<text x=\"108\" y=\"38\" font-family=\"Georgia,'Times New Roman',serif\" font-size=\"28\" font-style=\"italic\" font-weight=\"400\" fill=\"#0d1b2a\" letter-spacing=\"1\"> Ghassen</text>"
            + "<circle cx=\"104\" cy=\"34\" r=\"2\" fill=\"#C9A84C\" opacity=\"0.7\"/>"
            + "</svg>";
    }

    // ── CSS ───────────────────────────────────────────────────────────────────

    private String buildCss() {
        return "* { margin:0; padding:0; box-sizing:border-box; }"
            + "@page { size: 1122px 794px; margin: 0; }"
            + "body { font-family:'Helvetica Neue',Helvetica,Arial,sans-serif; background:#f5f0e8; width:1122px; height:794px; }"
            + ".page { width:1122px; height:794px; background: linear-gradient(160deg, #fdfaf4 0%, #f8f3e8 40%, #fdfaf4 100%); position:relative; overflow:hidden; }"
            + ".left-bar { position:absolute; left:0; top:0; bottom:0; width:18px; background: linear-gradient(180deg, #8B6914 0%, #C9A84C 25%, #F0D060 50%, #C9A84C 75%, #8B6914 100%); }"
            + ".top-band { position:absolute; top:0; left:18px; right:0; height:12px; background: linear-gradient(90deg, #0d1b2a 0%, #1a3a5c 50%, #0d1b2a 100%); }"
            + ".top-band-inner { position:absolute; bottom:0; left:0; right:0; height:3px; background: linear-gradient(90deg, transparent, #C9A84C 20%, #F0D060 50%, #C9A84C 80%, transparent); }"
            + ".bottom-band { position:absolute; bottom:0; left:18px; right:0; height:12px; background: linear-gradient(90deg, #0d1b2a 0%, #1a3a5c 50%, #0d1b2a 100%); }"
            + ".bottom-band-inner { position:absolute; top:0; left:0; right:0; height:3px; background: linear-gradient(90deg, transparent, #C9A84C 20%, #F0D060 50%, #C9A84C 80%, transparent); }"
            + ".corner-ornament { position:absolute; }"
            + ".corner-tl { top:12px; left:18px; } .corner-tr { top:12px; right:0; } .corner-bl { bottom:12px; left:18px; } .corner-br { bottom:12px; right:0; }"
            + ".watermark { position:absolute; top:50%; left:55%; transform:translate(-50%,-50%) rotate(-25deg); font-size:130px; font-weight:900; letter-spacing:12px; color:rgba(180,151,42,0.04); white-space:nowrap; pointer-events:none; }"
            + ".content { position:absolute; inset:0; left:18px; display:flex; flex-direction:column; align-items:center; justify-content:center; padding:28px 90px 20px; }"
            + ".brand-row { display:flex; align-items:center; gap:12px; margin-bottom:10px; }"
            + ".brand-logo { width:48px; height:48px; border-radius:50%; background: linear-gradient(135deg, #0d1b2a 0%, #1a3a5c 100%); display:flex; align-items:center; justify-content:center; color:#C9A84C; font-size:24px; font-weight:900; border:2px solid #C9A84C; }"
            + ".brand-name { font-size:26px; font-weight:800; color:#0d1b2a; letter-spacing:8px; text-transform:uppercase; }"
            + ".gold-line { width:420px; height:2px; margin:8px 0; background: linear-gradient(90deg, transparent, #C9A84C 20%, #F0D060 50%, #C9A84C 80%, transparent); }"
            + ".cert-label { font-size:10px; letter-spacing:7px; text-transform:uppercase; color:#8B6914; font-weight:600; margin-bottom:4px; }"
            + ".cert-title { font-size:62px; font-weight:300; color:#0d1b2a; letter-spacing:6px; text-transform:uppercase; line-height:1; font-style:italic; }"
            + ".cert-subtitle { font-size:11px; letter-spacing:10px; text-transform:uppercase; color:#C9A84C; font-weight:600; margin-top:2px; }"
            + ".thin-line { width:260px; height:1px; margin:10px 0; background: linear-gradient(90deg, transparent, #C9A84C, transparent); }"
            + ".presented-to { font-size:10px; letter-spacing:5px; text-transform:uppercase; color:#94a3b8; margin-bottom:4px; }"
            + ".recipient { font-size:50px; font-weight:600; color:#0d1b2a; font-style:italic; text-align:center; line-height:1.1; font-family:Georgia,'Times New Roman',serif; letter-spacing:1px; }"
            + ".completion-text { font-size:10px; letter-spacing:4px; text-transform:uppercase; color:#94a3b8; margin:10px 0 3px; }"
            + ".exam-title { font-size:20px; font-weight:700; color:#1a3a5c; text-align:center; letter-spacing:1px; }"
            + ".exam-category { font-size:10px; color:#94a3b8; letter-spacing:4px; text-transform:uppercase; margin-top:2px; }"
            + ".footer-row { width:100%; margin-top:26px; display:flex; align-items:flex-end; justify-content:space-between; gap:20px; }"
            + ".footer-col { display:flex; flex-direction:column; }"
            + ".footer-col--left { align-items:flex-start; flex:1; }"
            + ".footer-col--center { align-items:center; flex:1.2; }"
            + ".footer-col--right { align-items:flex-end; flex:1; }"
            + ".footer-label { font-size:8px; letter-spacing:3px; text-transform:uppercase; color:#94a3b8; margin-bottom:3px; }"
            + ".footer-value { font-size:13px; font-weight:700; color:#0d1b2a; }"
            + ".sig-wrap { margin-bottom:2px; }"
            + ".sig-line-rule { width:200px; height:1px; background:#0d1b2a; margin:0 auto 4px; }"
            + ".sig-name { font-size:13px; font-weight:700; color:#0d1b2a; text-align:center; letter-spacing:1px; }"
            + ".sig-title { font-size:8px; letter-spacing:2px; text-transform:uppercase; color:#8B6914; text-align:center; margin-top:2px; }"
            + ".cert-id { font-size:13px; font-weight:700; color:#0d1b2a; font-family:monospace; letter-spacing:1px; }";
    }
}
