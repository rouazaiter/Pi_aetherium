package com.education.platform.entities.portfolio;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_skill_normalized_name", columnNames = "normalized_name")
        }
)
@Getter
@Setter
@AllArgsConstructor
    @NoArgsConstructor
    @Builder

    public class Skill {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id  ;

        private String name ;
        @Column(name = "normalized_name", unique = true)
        private String normalizedName;
        private SkillCategory category  ;
        @Builder.Default//enum
        private  Boolean trendy = false  ;
        private String description ;
        @Builder.Default

        private Integer searchCount= 0;
        @ManyToMany(mappedBy = "skills")
        @Builder.Default
        private Set<PortfolioProject> projects = new HashSet<>();
        @ManyToMany(mappedBy = "skills")
        @Builder.Default
        private Set<Portfolio> portfolios = new HashSet<>();

        @PrePersist
        @PreUpdate
        public void syncNormalizedName() {
            this.normalizedName = normalizeName(this.name);
        }

        public static String normalizeName(String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim().toLowerCase();
            return normalized.isEmpty() ? null : normalized;
        }

        public void markAsTrendy() {
            this.trendy = true;
        }

        public void unmarkAsTrendy() {
            this.trendy = false;
        }
        public void incrementSearchCount() {
            if (searchCount == null) {
                searchCount = 0;
            }
            searchCount++;
            // Mark as trendy if search count exceeds 100
            if (searchCount > 100) {
                trendy = true;
            }
        }
    }


