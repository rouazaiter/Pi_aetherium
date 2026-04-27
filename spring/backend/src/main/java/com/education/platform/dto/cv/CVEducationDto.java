package com.education.platform.dto.cv;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CVEducationDto {

    @Size(max = 255)
    private String school;

    @Size(max = 255)
    private String degree;

    @Size(max = 255)
    private String fieldOfStudy;

    @Size(max = 255)
    private String location;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean current;

    @Size(max = 2000)
    private String description;
}
