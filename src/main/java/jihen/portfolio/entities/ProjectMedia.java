package jihen.portfolio.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jihen.portfolio.enums.MediaType;

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
    private String  mediaUrl ;
    @Enumerated(EnumType.STRING)

    private MediaType mediatype  ;
    private Integer orderIndex ;
    @CreationTimestamp
    private LocalDateTime createdAt  ;

    @ManyToOne
    private PortfolioProject project;

}
