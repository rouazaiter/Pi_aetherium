package com.education.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "DateOfsubscription", nullable = false)
    private LocalDate dateOfSubscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "SubscriptionPlan", nullable = false, length = 32)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "ExpirtationDate", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "BillingDate", nullable = false)
    private LocalDate billingDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "User_id", nullable = false)
    private User user;
}
