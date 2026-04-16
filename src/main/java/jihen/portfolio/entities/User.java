package jihen.portfolio.entities;


import jakarta.persistence.*;
import jihen.portfolio.enums.AccountStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

    @Entity
    @Table(name = "app_user")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true)
        private String username;

        @Column(nullable = false, unique = true)
        private String email;

        private String profilePicture;

        private String location;

        @Builder.Default
        private Boolean isVerified = false;

        @Builder.Default
        private Integer followerCount = 0;

        @Builder.Default
        private Integer totalViews = 0;

        @Builder.Default
        private Double trustScore = 0.0;

        @Enumerated(EnumType.STRING)
        private AccountStatus status ;

        @CreationTimestamp
        private LocalDateTime createdAt;
    }

