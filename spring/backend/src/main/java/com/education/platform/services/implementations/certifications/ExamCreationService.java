package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamCreationService {

    private final CertificationRepository certificationRepo;
    private final ExamRepository examRepo;
    private final QuestionRepository questionRepo;
    private final ChoiceRepository choiceRepo;

    @Transactional // Ensures that if one choice fails, the whole exam rolls back (no half-saved exams)
    public Exam createExamFromDTO(ExamCreationDTO dto) {

        // 1. Find the parent Certification
        Certification cert = certificationRepo.findById(dto.certificationId())
                .orElseThrow(() -> new RuntimeException("Certification not found with ID: " + dto.certificationId()));

        // 2. Map and Save the Exam
        Exam exam = Exam.builder()
                .title(dto.title())
                .timeLimit(dto.timeLimit())
                .passingScore(dto.passingScore())
                .certification(cert) // Attach parent
                .build();

        exam = examRepo.save(exam); // Save to generate the Exam ID

        // 3. Loop through Questions in the DTO
        for (QuestionDTO qDto : dto.questions()) {

            Question question = Question.builder()
                    .questionText(qDto.questionText())
                    .type(Question.QuestionType.valueOf(qDto.type())) // Convert String to Enum
                    .points(qDto.points())
                    .exam(exam) // Attach parent (the saved Exam)
                    .build();

            questionRepo.save(question);
        }

        return exam;
    }
}
