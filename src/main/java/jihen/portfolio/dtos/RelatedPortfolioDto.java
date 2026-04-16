package jihen.portfolio.dtos;

import lombok.*;

import java.util.Set;


@Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class RelatedPortfolioDto {

        private Long id;
        private String title;
        private String bio;
        private String profilePicture;
        private String location;
        private Integer similarityScore;
        private Set<String> matchedSkills;
    }

