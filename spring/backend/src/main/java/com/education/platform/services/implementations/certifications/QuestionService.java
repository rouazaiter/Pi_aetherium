package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepo;
    private final ChoiceRepository choiceRepo;
    private final CertificationService certificationService;

    public QuestionResponseDTO getById(Long id) {
        return certificationService.toQuestionDTO(findOrThrow(id));
    }

    @Transactional
    public QuestionResponseDTO update(Long id, QuestionCreateDTO dto) {
        Question q = findOrThrow(id);
        q.setQuestionText(dto.questionText());
        q.setType(dto.type());
        q.setPoints(dto.points());
        q.setOrderIndex(dto.orderIndex());
        q.setExpectedAnswer(dto.expectedAnswer());
        q.setCodeLanguage(dto.codeLanguage());

        // Persist options for MCQ/MULTI_SELECT/SCENARIO/ORDERING
        if (dto.options() != null && !dto.options().isEmpty()) {
            try {
                q.setOptions(new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(dto.options()));
            } catch (Exception ignored) {}
        } else {
            q.setOptions(null);
        }

        // Rebuild match choices for MATCH / DRAG_DROP
        if (dto.type() == Question.QuestionType.MATCH ||
            dto.type() == Question.QuestionType.DRAG_DROP) {
            choiceRepo.deleteAll(choiceRepo.findByQuestionId(id));
            if (dto.matchPairs() != null) {
                for (MatchPairDTO pair : dto.matchPairs()) {
                    choiceRepo.save(Choice.builder()
                            .matchLeft(pair.left())
                            .matchRight(pair.right())
                            .question(q)
                            .build());
                }
            }
        }

        return certificationService.toQuestionDTO(questionRepo.save(q));
    }

    @Transactional
    public void delete(Long id) {
        questionRepo.delete(findOrThrow(id));
    }

    private Question findOrThrow(Long id) {
        return questionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found: " + id));
    }
}
