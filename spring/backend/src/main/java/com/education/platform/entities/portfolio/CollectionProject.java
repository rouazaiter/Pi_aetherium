package com.education.platform.entities.portfolio;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class CollectionProject {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        private PortfolioCollection portfolioCollection;

        @ManyToOne(fetch = FetchType.LAZY)
        private PortfolioProject project;

        private LocalDateTime addedDate;

        private Integer orderIndex;
}
