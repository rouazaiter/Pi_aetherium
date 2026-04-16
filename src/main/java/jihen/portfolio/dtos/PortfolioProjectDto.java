package jihen.portfolio.dtos;
import java.time.LocalDateTime;
import java.util.List;

public class PortfolioProjectDto {


        private Long id;
        private String title;
        private String description;
        private String projectUrl;
        private Boolean isPinned;
        private Integer views;
        private Integer likes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PortfolioProjectDto() {}
        public PortfolioProjectDto(Long id, String title, String description, String projectUrl,
                                   Boolean isPinned, Integer views, Integer likes,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.projectUrl = projectUrl;
            this.isPinned = isPinned;
            this.views = views;
            this.likes = likes;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        private List<ProjectDto> featuredProjects;
        public String getProjectUrl() { return projectUrl; }
        public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }

        public Boolean getIsPinned() { return isPinned; }
        public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }

        public Integer getViews() { return views; }
        public void setViews(Integer views) { this.views = views; }

        public Integer getLikes() { return likes; }
        public void setLikes(Integer likes) { this.likes = likes; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

