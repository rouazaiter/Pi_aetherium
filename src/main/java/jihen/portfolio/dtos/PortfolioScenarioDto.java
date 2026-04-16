package jihen.portfolio.dtos;

import lombok.*;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PortfolioScenarioDto {
        private String scenarioKey;
        private String label;
        private String description;
    }

