package tn.esprit.backend.entities;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String interests;

    @Column(length = 2000)
    private String description;

    private String profilePicture;
    private LocalDateTime lastPasswordChanged;
    private String recuperationEmail;

    // ========== RELATIONS ==========

    // Relation 1-1 avec User (côté propriétaire)
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Relation 1-1 avec Address
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;
}