package jihen.portfolio.dto;


import java.util.List;
import jihen.portfolio.dtos.SkillNodeDto;
public class PortfolioDNADto {
    private Long portfolioId;
    private String portfolioTitle;
    private List<SkillNodeDto> skills;

    public PortfolioDNADto() {}

    public PortfolioDNADto(Long portfolioId, String portfolioTitle, List<SkillNodeDto> skills) {
        this.portfolioId = portfolioId;
        this.portfolioTitle = portfolioTitle;
        this.skills = skills;
    }

    // Getters et Setters
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }
    public String getPortfolioTitle() { return portfolioTitle; }
    public void setPortfolioTitle(String portfolioTitle) { this.portfolioTitle = portfolioTitle; }
    public List<SkillNodeDto> getSkills() { return skills; }
    public void setSkills(List<SkillNodeDto> skills) { this.skills = skills; }
}
