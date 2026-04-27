package com.education.platform.entities.portfolio;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
public class PortfolioCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;
    @Enumerated(EnumType.STRING)
    private Visibility visibility;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.ACTIVE;
    @Column(length = 1000)
    private String moderationReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Portfolio portfolio;

    @Builder.Default
    private Integer likes = 0;

    @OrderBy("addedDate DESC")
    @OneToMany(mappedBy = "portfolioCollection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CollectionProject> collectionProjects = new HashSet<>();


    public List<PortfolioProject> getProjects() {
        return collectionProjects.stream()
                .map(CollectionProject::getProject)
                .toList();
    }

    public void addProject(PortfolioProject project) {
        if (project == null) {
            return;
        }

        boolean exists = collectionProjects.stream().anyMatch(cp -> {
            PortfolioProject p = cp.getProject();
            if (p == null) return false;
            if (p.getId() != null && project.getId() != null) {
                return p.getId().equals(project.getId());
            }
            return p == project;
        });

        if (exists) {
            return;
        }

        CollectionProject link = CollectionProject.builder()
                .portfolioCollection(this)
                .project(project)
                .addedDate(LocalDateTime.now())
                .orderIndex(collectionProjects.size() + 1)
                .build();

        collectionProjects.add(link);
        project.getCollectionProjects().add(link);
    }

    public void removeProject(PortfolioProject project) {
        if (project == null) {
            return;
        }

        CollectionProject toRemove = collectionProjects.stream()
                .filter(cp -> {
                    PortfolioProject p = cp.getProject();
                    if (p == null) return false;
                    if (p.getId() != null && project.getId() != null) {
                        return p.getId().equals(project.getId());
                    }
                    return p == project;
                })
                .findFirst()
                .orElse(null);

        if (toRemove != null) {
            collectionProjects.remove(toRemove);
            project.getCollectionProjects().remove(toRemove);
            toRemove.setPortfolioCollection(null);
            toRemove.setProject(null);
        }
    }

    public void incrementLikes() {
        likes++;
    }

    public void decrementLikes() {
        if (likes > 0) {
            likes--;
        }
    }

}







