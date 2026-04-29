package com.education.platform.entities.cv;

import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.Visibility;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CVProfile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 255)
    private String headline;

    @Column(length = 3000)
    private String summary;

    @Column(length = 32)
    private String phone;

    @Column(length = 255)
    private String location;

    @Column(length = 100)
    private String preferredTemplate;

    @Column(length = 16)
    private String language;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "CVProfileSelectedProjects", joinColumns = @JoinColumn(name = "cv_profile_id"))
    @OrderColumn(name = "display_order")
    @Column(name = "project_id", nullable = false)
    @Builder.Default
    private List<Long> selectedProjectIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "CVProfileEducation", joinColumns = @JoinColumn(name = "cv_profile_id"))
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<CVEducationEntry> education = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "CVProfileExperience", joinColumns = @JoinColumn(name = "cv_profile_id"))
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<CVExperienceEntry> experience = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "CVProfileLanguages", joinColumns = @JoinColumn(name = "cv_profile_id"))
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<CVLanguageEntry> languages = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
