package com.education.platform.entities.cv;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cv_section")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id", nullable = false)
    private CVDraft draft;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private CVSectionType type;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "visible", nullable = false)
    private Boolean visible;

    @Column(name = "content_json", length = 20000, nullable = false)
    private String contentJson;
}
