package jihen.portfolio.dtos;

    public class CollectionDto {

        private Long id;
        private String name;
        private String description;
        private String previewImage;

        private Integer projectCount;

        public CollectionDto() {}

        public CollectionDto(Long id, String name, String description,
                             String previewImage, Integer projectCount) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.previewImage = previewImage;
            this.projectCount = projectCount;
        }

        // getters / setters
    }

