package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.dto.certifications.GradeRequest;
import com.education.platform.dto.certifications.GradeResponse;
import com.education.platform.services.implementations.certifications.GradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grading")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@RequestBody GradeRequest request) {
        try {
            GradeResponse result = gradingService.grade(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Grading failed: " + e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}
