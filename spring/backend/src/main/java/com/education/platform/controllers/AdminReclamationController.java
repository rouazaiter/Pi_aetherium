package com.education.platform.controllers;

import com.education.platform.dto.reclamation.AdminReclamationUpdateRequest;
import com.education.platform.dto.reclamation.ReclamationResponse;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.ReclamationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reclamations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReclamationController {

    private final CurrentUserService currentUserService;
    private final ReclamationService reclamationService;

    public AdminReclamationController(CurrentUserService currentUserService, ReclamationService reclamationService) {
        this.currentUserService = currentUserService;
        this.reclamationService = reclamationService;
    }

    @GetMapping
    public List<ReclamationResponse> listAll() {
        return reclamationService.listAllForAdmin();
    }

    @PatchMapping("/{id}")
    public ReclamationResponse update(@PathVariable Long id, @Valid @RequestBody AdminReclamationUpdateRequest request) {
        return reclamationService.updateByAdmin(currentUserService.getCurrentUser(), id, request);
    }
}
