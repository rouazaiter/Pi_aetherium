package jihen.portfolio.entities;

import lombok.*;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skillName;
    private Integer weight;

    @ManyToOne(fetch = FetchType.LAZY)
    private PortfolioScenario scenario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScenarioSkill)) return false;
        ScenarioSkill that = (ScenarioSkill) o;
        return Objects.equals(skillName, that.skillName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillName);
    }
}

