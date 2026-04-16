package jihen.portfolio.dtos;
import jihen.portfolio.enums.PortfolioVisibility;
import java.time.LocalDateTime;
public class PortfolioSimpleDto {


        private Long id;
        private Long userId;
        private String title;
        private String bio;
        private String profilePicture;
        private String coverImage;
        private String location;
        private PortfolioVisibility visibility;
        private Integer totalViews;
        private Boolean isVerified;
        private Integer followerCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructeurs
        public PortfolioSimpleDto() {}

        public PortfolioSimpleDto(Long id, Long userId, String title, String bio,
                                  String profilePicture, String coverImage, String location,
                                  PortfolioVisibility visibility, Integer totalViews,
                                  Boolean isVerified, Integer followerCount,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.bio = bio;
            this.profilePicture = profilePicture;
            this.coverImage = coverImage;
            this.location = location;
            this.visibility = visibility;
            this.totalViews = totalViews;
            this.isVerified = isVerified;
            this.followerCount = followerCount;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getProfilePicture() { return profilePicture; }
        public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public PortfolioVisibility getVisibility() { return visibility; }
        public void setVisibility(PortfolioVisibility visibility) { this.visibility = visibility; }

        public Integer getTotalViews() { return totalViews; }
        public void setTotalViews(Integer totalViews) { this.totalViews = totalViews; }

        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

        public Integer getFollowerCount() { return followerCount; }
        public void setFollowerCount(Integer followerCount) { this.followerCount = followerCount; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

