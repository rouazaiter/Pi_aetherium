package com.education.platform.services.interfaces;

import com.education.platform.dto.reclamation.AdminReclamationUpdateRequest;
import com.education.platform.dto.reclamation.CreateReclamationRequest;
import com.education.platform.dto.reclamation.ReclamationResponse;
import com.education.platform.entities.User;

import java.util.List;

public interface ReclamationService {

    ReclamationResponse create(User user, CreateReclamationRequest request);

    List<ReclamationResponse> listForUser(User user);

    ReclamationResponse getForUser(User user, Long id);

    List<ReclamationResponse> listAllForAdmin();

    ReclamationResponse updateByAdmin(User admin, Long id, AdminReclamationUpdateRequest request);
}
