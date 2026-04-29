package com.education.platform.repositories;

import com.education.platform.entities.FeedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedEventRepository extends JpaRepository<FeedEvent, Long> {

    List<FeedEvent> findTop200ByOrderByCreatedAtDesc();
}
