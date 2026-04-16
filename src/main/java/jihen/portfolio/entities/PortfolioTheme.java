package jihen.portfolio.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PortfolioTheme {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private Long portfolioId;

        private String name;

        private String type;

        private String primaryColor;

        private String secondaryColor;

        private String accentColor;

        private String fontFamily;

        private String backgroundStyle;

        private String backgroundImage;

        private Boolean isActive;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
            if (isActive == null) isActive = false;
        }

        @PreUpdate
        protected void onUpdate() {
            updatedAt = LocalDateTime.now();
        }
    }

