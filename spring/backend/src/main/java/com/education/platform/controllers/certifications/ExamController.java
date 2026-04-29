package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.dto.certifications.ExamCreationDTO;
import com.education.platform.entities.certifications.Exam;
import com.education.platform.services.implementations.certifications.ExamCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamCreationService examCreationService;

    @PostMapping("/create")
    public ResponseEntity<String> createExam(@RequestBody ExamCreationDTO dto) {
        try {
            Exam createdExam = examCreationService.createExamFromDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Exam '" + createdExam.getTitle() + "' successfully created with ID: " + createdExam.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating exam: " + e.getMessage());
        }
    }
}
