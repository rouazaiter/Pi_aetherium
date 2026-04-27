package com.education.platform.entities.portfolio;

import com.education.platform.entities.User;
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
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String bio;

    @Column(length = 2000)
    private String coverImage; // public portfolio cover URL

    @Enumerated(EnumType.STRING)
    private Visibility visibility; // PUBLIC / PRIVATE / etc.
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.ACTIVE;
    @Column(length = 1000)
    private String moderationReason;
    @Builder.Default
    private Long totalViews = 0L;

    @Builder.Default
    private boolean verified = false;



    private String job; // renamed from professionalTitle

    @Column(length = 1000)
    private String githubUrl;

    @Column(length = 1000)
    private String linkedinUrl;

    @Builder.Default
    private boolean openToWork = false;

    @Builder.Default
    private boolean availableForFreelance = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PortfolioProject> projects = new HashSet<>();

    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PortfolioCollection> collections =  new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public void addSkill(Skill skill) {
        if (skill != null) {
            this.skills.add(skill);
        }
    }
    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
    }

    public void addProject(PortfolioProject project) {
        if (project != null) {
            this.projects.add(project);
            project.setPortfolio(this);
        }
    }

    public void removeProject(PortfolioProject project) {
        if (project != null) {
            this.projects.remove(project);
            project.setPortfolio(null);
        }
    }
    public void addCollection(PortfolioCollection collection) {
        if (collection != null) {
            collections.add(collection);
            collection.setPortfolio(this);
        }
    }

    public void removeCollection(PortfolioCollection collection) {
        if (collection != null) {
            collections.remove(collection);
            collection.setPortfolio(null);
        }
    }

}
