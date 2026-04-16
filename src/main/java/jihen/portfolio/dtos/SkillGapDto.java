package jihen.portfolio.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.List;

    @Data
    @Builder
    public class SkillGapDto {
        private Long portfolioId;
        private String portfolioTitle;
        private Integer completenessScore;
        private List<CurrentSkillDto> currentSkills;
        private List<MissingSkillDto> missingSkills;
        private List<String> recommendations;

        @Data
        @Builder
        public static class CurrentSkillDto {
            private String name;
            private String category;
            private Integer proficiency;
        }

        @Data
        @Builder
        public static class MissingSkillDto {
            private String name;
            private String category;
            private Integer trendingScore;
            private String potentialGain;
            private String reason;
        }
    }

