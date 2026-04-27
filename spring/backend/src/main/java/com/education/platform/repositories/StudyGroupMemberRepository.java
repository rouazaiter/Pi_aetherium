package com.education.platform.repositories;

import com.education.platform.entities.StudyGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, Long> {

    Optional<StudyGroupMember> findByGroup_IdAndUser_Id(Long groupId, Long userId);

    List<StudyGroupMember> findByUser_Id(Long userId);

    List<StudyGroupMember> findByGroup_Id(Long groupId);
}
