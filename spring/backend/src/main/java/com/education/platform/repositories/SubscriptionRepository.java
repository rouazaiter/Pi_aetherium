package com.education.platform.repositories;

import com.education.platform.entities.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUser_IdOrderByDateOfSubscriptionDesc(Long userId);
}
