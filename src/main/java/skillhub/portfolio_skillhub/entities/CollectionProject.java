package skillhub.portfolio_skillhub.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CollectionProject {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;


     @ManyToOne(fetch = FetchType.EAGER)
     private Collection collection;
     @ManyToOne(fetch = FetchType.EAGER)
     private PortfolioProject project;
     private LocalDateTime addedDate;

     private Integer orderIndex;
}