package jihen.portfolio.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class ThemeResponseDto {
        private Long id;
        private String name;
        private String type;
        private String primaryColor;
        private String secondaryColor;
        private String accentColor;
        private String fontFamily;
        private String backgroundStyle;
        private String backgroundImage;
        private Boolean isActive;
    }

