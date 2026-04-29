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
public class CVExperienceDto {

    @Size(max = 255)
    private String company;

    @Size(max = 255)
    private String role;

    @Size(max = 255)
    private String location;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean current;

    @Size(max = 2000)
    private String summary;
}
