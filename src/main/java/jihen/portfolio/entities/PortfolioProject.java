package jihen.portfolio.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ; // pk
    private String title ;
    private String description ;
    private String projectUrl ;
    private Boolean pinned ;
    private Integer views ;
    private Integer likes ;
    @CreationTimestamp
    LocalDateTime createdAt ;
    @UpdateTimestamp
    LocalDateTime  updatedAt ;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMedia> media = new ArrayList<>();
    @ManyToOne
    private Portfolio portfolio;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("addedDate DESC")
    private Set<CollectionProject> collectionProjects = new HashSet<>();
    @ManyToMany
    private Set<Skill> skills = new HashSet<>();

    public void addMedia(ProjectMedia mediaItem) {
        media.add(mediaItem);
        mediaItem.setProject(this);
    }

    public void removeMedia(ProjectMedia mediaItem) {
        media.remove(mediaItem);
        mediaItem.setProject(null);
    }
    public List<Collection> getCollections() {
        return this.collectionProjects.stream()
                .map(CollectionProject::getCollection)
                .toList();
    }

    public void incrementViews() {
        if (views == null) views = 0;
        views++;
    }

    public void incrementLikes() {
        if (likes == null) likes = 0;
        likes++;
    }
}
