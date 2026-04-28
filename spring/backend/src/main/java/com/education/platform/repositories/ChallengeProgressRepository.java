package com.education.platform.repositories;

import com.education.platform.entities.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {

    Optional<ChallengeProgress> findByChallenge_IdAndUser_Id(Long challengeId, Long userId);

    List<ChallengeProgress> findByChallenge_IdOrderByPointsDesc(Long challengeId);
}
