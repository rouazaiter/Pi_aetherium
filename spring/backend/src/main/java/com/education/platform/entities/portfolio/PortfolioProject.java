package com.education.platform.entities.portfolio;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 2000)
    private String projectUrl;

    @Builder.Default
    private Boolean pinned = false;

    @Builder.Default
    private Integer views = 0;

    @Builder.Default
    private Integer likes = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.ACTIVE;
    @Column(length = 1000)
    private String moderationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    private Portfolio portfolio;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("addedDate DESC")
    @Builder.Default
    private Set<CollectionProject> collectionProjects = new HashSet<>();

    @ManyToMany
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    public void addMedia(ProjectMedia mediaItem) {
        if (mediaItem != null) {
            media.add(mediaItem);
            mediaItem.setProject(this);
        }
    }

    public void removeMedia(ProjectMedia mediaItem) {
        if (mediaItem != null) {
            media.remove(mediaItem);
            mediaItem.setProject(null);
        }
    }
}
