package com.education.platform.entities.cv;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVLanguageEntry {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "proficiency", length = 100)
    private String proficiency;
}
