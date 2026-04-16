package jihen.portfolio.dtos;

import lombok.*;
import java.util.List;

public class SkillNodeDto {
    private Long id;
    private String name;
    private String category;
    private Boolean isTrendy;
    private List<SkillNodeDto> subSkills;  // Sous-compétences suggérées

    public SkillNodeDto() {}

    public SkillNodeDto(Long id, String name, String category, Boolean isTrendy, List<SkillNodeDto> subSkills) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.isTrendy = isTrendy;
        this.subSkills = subSkills;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getIsTrendy() { return isTrendy; }
    public void setIsTrendy(Boolean isTrendy) { this.isTrendy = isTrendy; }
    public List<SkillNodeDto> getSubSkills() { return subSkills; }
    public void setSubSkills(List<SkillNodeDto> subSkills) { this.subSkills = subSkills; }
}
