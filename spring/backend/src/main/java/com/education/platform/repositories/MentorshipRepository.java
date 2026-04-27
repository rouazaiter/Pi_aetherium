package com.education.platform.repositories;

import com.education.platform.entities.Mentorship;
import com.education.platform.entities.MentorshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MentorshipRepository extends JpaRepository<Mentorship, Long> {

    Optional<Mentorship> findByIdAndMentor_Id(Long id, Long mentorId);

    Optional<Mentorship> findByMentor_IdAndMentee_IdAndStatus(Long mentorId, Long menteeId, MentorshipStatus status);

    List<Mentorship> findByMentor_IdOrMentee_Id(Long mentorId, Long menteeId);

    List<Mentorship> findByMentor_IdAndStatus(Long mentorId, MentorshipStatus status);
}
