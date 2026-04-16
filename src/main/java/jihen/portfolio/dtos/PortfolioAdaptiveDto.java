package jihen.portfolio.dtos;

import jihen.portfolio.entities.Portfolio;
import java.util.List;
public class PortfolioAdaptiveDto {



        private PortfolioSimpleDto portfolio;  // ← Changé: PortfolioSimpleDTO au lieu de Portfolio
        private List<PortfolioProjectDto> projects;
        private String visitorType;
        private String priority;
        private int totalProjects;

        // Constructeurs
        public PortfolioAdaptiveDto() {}

        public PortfolioAdaptiveDto(PortfolioSimpleDto portfolio, List<PortfolioProjectDto> projects,
                                    String visitorType, String priority) {
            this.portfolio = portfolio;
            this.projects = projects;
            this.visitorType = visitorType;
            this.priority = priority;
            this.totalProjects = projects != null ? projects.size() : 0;
        }

        // Getters et Setters
        public PortfolioSimpleDto getPortfolio() { return portfolio; }
        public void setPortfolio(PortfolioSimpleDto portfolio) { this.portfolio = portfolio; }

        public List<PortfolioProjectDto> getProjects() { return projects; }
        public void setProjects(List<PortfolioProjectDto> projects) { this.projects = projects; }

        public String getVisitorType() { return visitorType; }
        public void setVisitorType(String visitorType) { this.visitorType = visitorType; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public int getTotalProjects() { return totalProjects; }
        public void setTotalProjects(int totalProjects) { this.totalProjects = totalProjects; }
    }






