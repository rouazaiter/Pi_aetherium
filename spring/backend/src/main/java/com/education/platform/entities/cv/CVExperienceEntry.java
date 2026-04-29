package com.education.platform.entities.cv;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVExperienceEntry {

    @Column(name = "company", length = 255)
    private String company;

    @Column(name = "role", length = 255)
    private String role;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "current_role")
    private Boolean current;

    @Column(name = "summary", length = 2000)
    private String summary;
}
