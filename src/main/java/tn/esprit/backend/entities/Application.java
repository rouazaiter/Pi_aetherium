epackage tn.esprit.backend.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"service_request_id", "applicant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    // Relation N-1 avec ServiceRequest (la demande a laquelle on postule)
    @ManyToOne
    @JoinColumn(name = "service_request_id", nullable = false)
    @JsonIgnore
    private ServiceRequest serviceRequest;

    // Relation N-1 avec User (le candidat)
    @ManyToOne
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }
}