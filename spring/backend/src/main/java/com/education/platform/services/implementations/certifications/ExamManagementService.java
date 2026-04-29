package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamManagementService {

    private final CertificationRepository certificationRepo;
    private final ExamRepository examRepo;
    private final QuestionRepository questionRepo;
    private final ChoiceRepository choiceRepo;

    /**
     * This demonstrates how to manually save an exam step-by-step
     * using your exact current entity structure.
     */
    @Transactional
    public void createDemoExam() {

        // 1. Create and Save the Certification
        Certification cert = Certification.builder()
                .title("Full-Stack JS Developer")
                .description("Master Angular and Node.js")
                .difficulty(Certification.Difficulty.INTERMEDIATE)
                .build();
        cert = certificationRepo.save(cert); // Save it to get the ID

        // 2. Create and Save the Exam (Link it to the Certification)
        Exam exam = Exam.builder()
                .title("Angular Foundations Exam")
                .timeLimit(60)
                .passingScore(70.0)
                .certification(cert) // Attach parent
                .build();
        exam = examRepo.save(exam); // Save it to get the ID

        // 3. Create and Save a Question (Link it to the Exam)
        Question q1 = Question.builder()
                .questionText("What directive is used for loops in Angular?")
                .type(Question.QuestionType.EXPLAIN)
                .points(10.0)
                .exam(exam) // Attach parent
                .build();
        q1 = questionRepo.save(q1); // Save it to get the ID

        // 4. Create and Save the Choices (Link them to the Question)
        Choice choice1 = Choice.builder()
                .text("*ngIf")
                .isCorrect(false)
                .question(q1) // Attach parent
                .build();

        Choice choice2 = Choice.builder()
                .text("*ngFor")
                .isCorrect(true)
                .question(q1) // Attach parent
                .build();

        choiceRepo.save(choice1);
        choiceRepo.save(choice2);

        System.out.println("Demo Exam successfully created and saved to the database!");
    }
}
