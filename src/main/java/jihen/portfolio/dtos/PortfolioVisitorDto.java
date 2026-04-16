package jihen.portfolio.dtos;

import java.util.List;

    public class PortfolioVisitorDto {

        private Long id;
        private String title;
        private String bio;
        private String profilePicture;
        private String coverImage;
        private String location;

        // 🔥 USER INFO
        private Long userId;
        private String username;
        private Boolean isVerified;
        private Integer followerCount;
        private Double trustScore;

        // 🔥 STATS
        private Integer totalViews;
        private Integer totalProjects;
        private Integer totalCollections;
        private Integer totalLikes;

        // 🔥 CONTENT
        private List<ProjectDto> featuredProjects;
        private List<CollectionDto> collections;
        private List<String> skills;

        // 🔥 DISCOVERY
        private List<RelatedPortfolioDto> relatedPortfolios;
    }

