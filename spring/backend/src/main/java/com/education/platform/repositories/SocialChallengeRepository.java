package com.education.platform.repositories;

import com.education.platform.entities.SocialChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocialChallengeRepository extends JpaRepository<SocialChallenge, Long> {

    List<SocialChallenge> findByGroup_IdOrderByStartDateDesc(Long groupId);
}
