package jihen.portfolio.dtos;


import org.springframework.web.multipart.MultipartFile;

    public class ImageUploadDto {
        private MultipartFile profilePicture;
        private MultipartFile coverImage;

        // Getters et Setters
        public MultipartFile getProfilePicture() { return profilePicture; }
        public void setProfilePicture(MultipartFile profilePicture) { this.profilePicture = profilePicture; }

        public MultipartFile getCoverImage() { return coverImage; }
        public void setCoverImage(MultipartFile coverImage) { this.coverImage = coverImage; }
    }

