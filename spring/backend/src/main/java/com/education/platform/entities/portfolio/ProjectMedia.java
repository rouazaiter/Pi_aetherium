package com.education.platform.entities.portfolio;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
    @Entity
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder

    public class ProjectMedia {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id  ;
        @Column(length = 2000)
        private String  mediaUrl ;
        @Enumerated(EnumType.STRING)

        private MediaType mediaType  ;
        @Builder.Default
        private Integer orderIndex = 0;
        @CreationTimestamp
        private LocalDateTime createdAt  ;

        @ManyToOne(fetch = FetchType.LAZY)
        private PortfolioProject project;

    }

