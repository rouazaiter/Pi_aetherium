package jihen.portfolio.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private Portfolio portfolio;

    @OrderBy("addedDate DESC")
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CollectionProject> collectionProjects = new HashSet<>();

    public void addProject(PortfolioProject project) {
        CollectionProject collectionProject = CollectionProject.builder()
                .collection(this)
                .project(project)
                .addedDate(LocalDateTime.now())
                .orderIndex(this.collectionProjects.size() + 1)
                .build();
        this.collectionProjects.add(collectionProject);
        project.getCollectionProjects().add(collectionProject);
    }

    public void removeProject(PortfolioProject project) {
        CollectionProject toRemove = this.collectionProjects.stream()
                .filter(cp -> cp.getProject().equals(project))
                .findFirst()
                .orElse(null);
        if (toRemove != null) {
            this.collectionProjects.remove(toRemove);
            project.getCollectionProjects().remove(toRemove);
            toRemove.setCollection(null);
            toRemove.setProject(null);

        }
    }

    public List<PortfolioProject> getProjects() {
        return this.collectionProjects.stream()
                .map(CollectionProject::getProject)
                .toList();
    }
}





