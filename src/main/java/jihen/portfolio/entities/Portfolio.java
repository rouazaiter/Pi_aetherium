package jihen.portfolio.entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jihen.portfolio.enums.PortfolioVisibility;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private String title;
    private String bio ;
    private String profilePicture ; // url
    private String coverImage ;// url
    private String location ;
    @Enumerated(EnumType.STRING)
    private PortfolioVisibility visibility ;//enum
    private Integer totalViews ;
    private Boolean isVerified ;
    private  Integer followerCount ;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt ;


    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Skill> skills = new HashSet<>();

    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PortfolioProject> projects = new HashSet<>();

    @OrderBy("createdAt DESC")
    @OneToMany( mappedBy = "portfolio",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Collection> collections = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    public void addSkill(Skill skill) {
        if (!skills.contains(skill)) {
            skills.add(skill);
        }
    }

    public void removeSkill(Skill skill) {
        skills.remove(skill);
    }

    public void addProject(PortfolioProject project) {
        projects.add(project);
        project.setPortfolio(this);
    }

    public void removeProject(PortfolioProject project) {
        projects.remove(project);
        project.setPortfolio(null);
    }

    public void addCollection(Collection collection) {
        collections.add(collection);
        collection.setPortfolio(this);
    }

    public void removeCollection(Collection collection) {
        collections.remove(collection);
        collection.setPortfolio(null);
    }

    public void incrementViews() {
        if (totalViews == null) {
            totalViews = 0;
        }
        totalViews++;
    }

}
