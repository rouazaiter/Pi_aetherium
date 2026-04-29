package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.dto.certifications.*;
import com.education.platform.services.implementations.certifications.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponseDTO> update(@PathVariable Long id,
                                                      @RequestBody QuestionCreateDTO dto) {
        return ResponseEntity.ok(questionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
