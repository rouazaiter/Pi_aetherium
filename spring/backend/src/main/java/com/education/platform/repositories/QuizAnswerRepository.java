package com.education.platform.repositories;

import com.education.platform.entities.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    Optional<QuizAnswer> findByQuiz_IdAndUser_Id(Long quizId, Long userId);

    long countByQuiz_Id(Long quizId);

    long countByQuiz_IdAndSelectedOption(Long quizId, String selectedOption);

    List<QuizAnswer> findByQuiz_Group_Id(Long groupId);
}
