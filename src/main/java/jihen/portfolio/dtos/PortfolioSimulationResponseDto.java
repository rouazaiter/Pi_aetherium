package jihen.portfolio.dtos;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSimulationResponseDto {
    private String scenarioKey;
    private String label;
    private String description;
    private Integer globalScore;
    private String reason;
    private Set<SimulationSkillDto> recommendedSkills;
    private Set<SimulatedProjectDto> recommendedProjects;
}


