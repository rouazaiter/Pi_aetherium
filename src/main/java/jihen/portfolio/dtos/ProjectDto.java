package jihen.portfolio.dtos;

import java.time.LocalDate;

    public class ProjectDto {

        private Long id;
        private String title;
        private String description;
        private String imageUrl;

        private Integer likes;
        private Integer views;

        private Boolean isPinned;

        // Optional (very useful later)
        private LocalDate startDate;
        private LocalDate endDate;

        public ProjectDto() {}

        public ProjectDto(Long id, String title, String description, String imageUrl,
                          Integer likes, Integer views, Boolean isPinned) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.likes = likes;
            this.views = views;
            this.isPinned = isPinned;
        }

        // getters / setters
    }

