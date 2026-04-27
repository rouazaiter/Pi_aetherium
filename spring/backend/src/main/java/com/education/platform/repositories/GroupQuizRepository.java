package com.education.platform.repositories;

import com.education.platform.entities.GroupQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupQuizRepository extends JpaRepository<GroupQuiz, Long> {

    List<GroupQuiz> findByGroup_IdOrderByCreatedAtDesc(Long groupId);
}
