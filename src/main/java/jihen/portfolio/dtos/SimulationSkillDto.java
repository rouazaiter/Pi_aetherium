package jihen.portfolio.dtos;

import lombok.*;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class SimulationSkillDto {
        private Long id;
        private String name;
        private String category;
        private Integer weight;
    }

