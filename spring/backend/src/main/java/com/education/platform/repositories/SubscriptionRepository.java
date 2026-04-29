package com.education.platform.repositories;

import com.education.platform.entities.Subscription;
import com.education.platform.entities.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUser_IdOrderByDateOfSubscriptionDesc(Long userId);

    Optional<Subscription> findFirstByUser_IdAndStatusOrderByDateOfSubscriptionDesc(Long userId, SubscriptionStatus status);

    Optional<Subscription> findByIdAndUser_Id(Long id, Long userId);
}
