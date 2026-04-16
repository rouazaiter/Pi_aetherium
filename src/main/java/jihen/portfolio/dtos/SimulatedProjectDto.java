package jihen.portfolio.dtos;

import lombok.*;

import java.util.Set;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class SimulatedProjectDto {
        private Long id;
        private String title;
        private String description;
        private String projectUrl;
        private Integer score;
        private Set<String> matchedSkills;
    }

