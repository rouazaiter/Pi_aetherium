package com.education.platform.services.implementations;

import com.education.platform.dto.reclamation.AdminReclamationUpdateRequest;
import com.education.platform.dto.reclamation.CreateReclamationRequest;
import com.education.platform.dto.reclamation.ReclamationResponse;
import com.education.platform.entities.Reclamation;
import com.education.platform.entities.ReclamationStatus;
import com.education.platform.entities.User;
import com.education.platform.repositories.ReclamationRepository;
import com.education.platform.services.interfaces.ReclamationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ReclamationServiceImpl implements ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final AccountMailService accountMailService;

    @Value("${app.reclamations.notify-email:}")
    private String reclamationNotifyEmail;

    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public ReclamationServiceImpl(ReclamationRepository reclamationRepository, AccountMailService accountMailService) {
        this.reclamationRepository = reclamationRepository;
        this.accountMailService = accountMailService;
    }

    @Override
    @Transactional
    public ReclamationResponse create(User user, CreateReclamationRequest request) {
        Instant now = Instant.now();
        Reclamation r = Reclamation.builder()
                .user(user)
                .subject(request.getSubject().trim())
                .description(request.getDescription().trim())
                .status(ReclamationStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        reclamationRepository.save(r);
        if (StringUtils.hasText(reclamationNotifyEmail)) {
            String base = frontendBaseUrl != null ? frontendBaseUrl.trim().replaceAll("/+$", "") : "";
            String adminUrl = base.isEmpty() ? "" : base + "/admin/reclamations";
            accountMailService.sendReclamationSubmittedToAdmin(
                    reclamationNotifyEmail.trim(),
                    user.getUsername(),
                    user.getEmail(),
                    r.getId(),
                    r.getSubject(),
                    r.getDescription(),
                    adminUrl);
        }
        return toUserResponse(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> listForUser(User user) {
        return reclamationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationResponse getForUser(User user, Long id) {
        Reclamation r = reclamationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));
        return toUserResponse(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> listAllForAdmin() {
        return reclamationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReclamationResponse updateByAdmin(User admin, Long id, AdminReclamationUpdateRequest request) {
        Reclamation r = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));
        ReclamationStatus next = request.getStatus();
        if (next != ReclamationStatus.IN_REVIEW && next != ReclamationStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut non autorisé pour cette action");
        }
        String msg = request.getAdminResponse() != null ? request.getAdminResponse().trim() : null;
        if (next == ReclamationStatus.RESOLVED && (msg == null || msg.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une réponse au client est requise pour clôturer");
        }
        Instant now = Instant.now();
        r.setStatus(next);
        r.setAdminResponse(msg != null && !msg.isEmpty() ? msg : r.getAdminResponse());
        r.setUpdatedAt(now);
        r.setReviewedAt(now);
        r.setReviewedBy(admin);
        reclamationRepository.save(r);
        return toAdminResponse(r);
    }

    private ReclamationResponse toUserResponse(Reclamation r) {
        return ReclamationResponse.builder()
                .id(r.getId())
                .submitterUsername(null)
                .subject(r.getSubject())
                .description(r.getDescription())
                .status(r.getStatus())
                .adminResponse(r.getAdminResponse())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .reviewedAt(r.getReviewedAt())
                .build();
    }

    private ReclamationResponse toAdminResponse(Reclamation r) {
        return ReclamationResponse.builder()
                .id(r.getId())
                .submitterUsername(r.getUser().getUsername())
                .subject(r.getSubject())
                .description(r.getDescription())
                .status(r.getStatus())
                .adminResponse(r.getAdminResponse())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .reviewedAt(r.getReviewedAt())
                .build();
    }
}
