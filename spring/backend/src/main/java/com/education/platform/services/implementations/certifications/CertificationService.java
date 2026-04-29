package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationRepository certificationRepo;
    private final ExamRepository examRepo;
    private final QuestionRepository questionRepo;
    private final ChoiceRepository choiceRepo;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public List<CertificationDTO> getAll() {
        return certificationRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CertificationDTO> getPublished() {
        return certificationRepo.findByStatus(Certification.Status.PUBLISHED)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CertificationDTO> search(String title) {
        return certificationRepo.findByTitleContainingIgnoreCase(title)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CertificationDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    public CertificationDetailDTO getDetail(Long id) {
        Certification cert = findOrThrow(id);
        return toDetailDTO(cert);
    }

    @Transactional
    public CertificationDTO create(CertificationCreateDTO dto) {
        Certification cert = Certification.builder()
                .title(dto.title())
                .description(dto.description())
                .category(dto.category())
                .difficulty(dto.difficulty())
                .status(dto.status() != null ? dto.status() : Certification.Status.DRAFT)
                .price(dto.price())
                .validFrom(dto.validFrom())
                .expiresAt(dto.expiresAt())
                .durationMinutes(dto.durationMinutes())
                .passingScore(dto.passingScore())
                .coverImageUrl(dto.coverImageUrl())
                .build();

        cert = certificationRepo.save(cert);

        if (dto.exams() != null) {
            for (ExamCreateDTO examDto : dto.exams()) {
                saveExamWithQuestions(examDto, cert);
            }
        }

        return toDTO(cert);
    }

    @Transactional
    public CertificationDTO update(Long id, CertificationCreateDTO dto) {
        Certification cert = findOrThrow(id);
        cert.setTitle(dto.title());
        cert.setDescription(dto.description());
        cert.setCategory(dto.category());
        cert.setDifficulty(dto.difficulty());
        if (dto.status() != null) cert.setStatus(dto.status());
        cert.setPrice(dto.price());
        cert.setValidFrom(dto.validFrom());
        cert.setExpiresAt(dto.expiresAt());
        cert.setDurationMinutes(dto.durationMinutes());
        cert.setPassingScore(dto.passingScore());
        if (dto.coverImageUrl() != null) cert.setCoverImageUrl(dto.coverImageUrl());
        return toDTO(certificationRepo.save(cert));
    }

    @Transactional
    public void delete(Long id) {
        certificationRepo.delete(findOrThrow(id));
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private Certification findOrThrow(Long id) {
        return certificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Certification not found: " + id));
    }

    public Exam saveExamWithQuestions(ExamCreateDTO dto, Certification cert) {
        Exam exam = Exam.builder()
                .title(dto.title())
                .timeLimit(dto.timeLimit())
                .passingScore(dto.passingScore())
                .certification(cert)
                .build();
        exam = examRepo.save(exam);

        if (dto.questions() != null) {
            for (QuestionCreateDTO qDto : dto.questions()) {
                saveQuestion(qDto, exam);
            }
        }
        return exam;
    }

    private void saveQuestion(QuestionCreateDTO qDto, Exam exam) {
        // Serialize options list to JSON string if present
        String optionsJson = null;
        if (qDto.options() != null && !qDto.options().isEmpty()) {
            try {
                optionsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(qDto.options());
            } catch (Exception ignored) {}
        }

        Question question = Question.builder()
                .questionText(qDto.questionText())
                .type(qDto.type())
                .points(qDto.points())
                .orderIndex(qDto.orderIndex())
                .expectedAnswer(qDto.expectedAnswer())
                .codeLanguage(qDto.codeLanguage())
                .options(optionsJson)
                .exam(exam)
                .build();
        question = questionRepo.save(question);

        // Save match pairs as Choice rows for MATCH and DRAG_DROP
        if (qDto.matchPairs() != null &&
            (qDto.type() == Question.QuestionType.MATCH ||
             qDto.type() == Question.QuestionType.DRAG_DROP)) {
            for (MatchPairDTO pair : qDto.matchPairs()) {
                Choice choice = Choice.builder()
                        .matchLeft(pair.left())
                        .matchRight(pair.right())
                        .question(question)
                        .build();
                choiceRepo.save(choice);
            }
        }
    }

    public CertificationDTO toDTO(Certification c) {
        return new CertificationDTO(
                c.getId(), c.getTitle(), c.getDescription(), c.getCategory(),
                c.getDifficulty(), c.getStatus(), c.getPrice(),
                c.getValidFrom(), c.getExpiresAt(), c.getDurationMinutes(),
                c.getPassingScore(), c.getCreatedAt(), c.getUpdatedAt(),
                c.getCoverImageUrl()
        );
    }

    public CertificationDetailDTO toDetailDTO(Certification c) {
        List<ExamResponseDTO> examDTOs = examRepo.findByCertificationId(c.getId()).stream()
                .map(exam -> new ExamResponseDTO(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getTimeLimit(),
                        exam.getPassingScore(),
                        questionRepo.findByExamIdOrderByOrderIndexAsc(exam.getId()).stream()
                                .map(this::toQuestionDTO)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new CertificationDetailDTO(
                c.getId(), c.getTitle(), c.getDescription(), c.getCategory(),
                c.getDifficulty(), c.getStatus(), c.getPrice(),
                c.getValidFrom(), c.getExpiresAt(), c.getDurationMinutes(),
                c.getPassingScore(), c.getCreatedAt(), c.getUpdatedAt(),
                examDTOs, c.getCoverImageUrl()
        );
    }

    public QuestionResponseDTO toQuestionDTO(Question q) {
        List<ChoiceResponseDTO> choices = choiceRepo.findByQuestionId(q.getId()).stream()
                .map(ch -> new ChoiceResponseDTO(ch.getId(), ch.getMatchLeft(), ch.getMatchRight(), ch.getText(), ch.isCorrect()))
                .collect(Collectors.toList());

        // Deserialize options JSON back to list
        List<String> options = null;
        if (q.getOptions() != null && !q.getOptions().isBlank()) {
            try {
                options = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(q.getOptions(),
                                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }

        return new QuestionResponseDTO(
                q.getId(), q.getType(), q.getQuestionText(),
                q.getExpectedAnswer(), q.getCodeLanguage(),
                q.getPoints(), q.getOrderIndex(), choices, options
        );
    }
}
