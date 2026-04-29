package com.education.platform.controllers;

import com.education.platform.dto.reclamation.CreateReclamationRequest;
import com.education.platform.dto.reclamation.ReclamationResponse;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.ReclamationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/social/reclamations", "/api/reclamations"})
public class ReclamationController {

    private final CurrentUserService currentUserService;
    private final ReclamationService reclamationService;

    public ReclamationController(CurrentUserService currentUserService, ReclamationService reclamationService) {
        this.currentUserService = currentUserService;
        this.reclamationService = reclamationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReclamationResponse create(@Valid @RequestBody CreateReclamationRequest request) {
        return reclamationService.create(currentUserService.getCurrentUser(), request);
    }

    /** Liste des réclamations de l'utilisateur connecté (évite le segment `/me` confondu avec `/{id}`). */
    @GetMapping
    public List<ReclamationResponse> mine() {
        return reclamationService.listForUser(currentUserService.getCurrentUser());
    }

    @GetMapping("/{id}")
    public ReclamationResponse one(@PathVariable Long id) {
        return reclamationService.getForUser(currentUserService.getCurrentUser(), id);
    }
}
