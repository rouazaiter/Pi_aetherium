package jihen.portfolio.entities;

import jakarta.persistence.*;
import lombok.*;
import jihen.portfolio.enums.SkillCategory;

import java.util.HashSet;
import java.util.Set;

@Entity
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
    @Enumerated(EnumType.STRING)
    private SkillCategory typecategory  ;//enum
    private  Boolean isTrendy ;
    private String description ;
    private Integer searchCount;
    @ManyToMany(mappedBy = "skills")
    private Set<PortfolioProject> projects = new HashSet<>();
    public void markAsTrendy() {
        this.isTrendy = true;
    }

    public void unmarkAsTrendy() {
        this.isTrendy = false;
    }
    public void incrementSearchCount() {
        if (searchCount == null) {
            searchCount = 0;
        }
        searchCount++;
        // Mark as trendy if search count exceeds 100
        if (searchCount > 100) {
            isTrendy = true;
        }
    }
}
