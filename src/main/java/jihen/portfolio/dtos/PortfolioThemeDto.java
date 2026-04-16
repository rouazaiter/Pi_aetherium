package jihen.portfolio.dtos;

public class PortfolioThemeDto {

        private String domain;      // DEVELOPMENT, DESIGN, DATA, MARKETING, etc.
        private String themeName;   // Dark Tech, Creative, Data, Dynamic
        private String primaryColor;
        private String secondaryColor;
        private String accentColor;

        public PortfolioThemeDto() {}

        public PortfolioThemeDto(String domain, String themeName, String primaryColor,
                                 String secondaryColor, String accentColor) {
            this.domain = domain;
            this.themeName = themeName;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
            this.accentColor = accentColor;
        }

        // Getters et Setters
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }

        public String getThemeName() { return themeName; }
        public void setThemeName(String themeName) { this.themeName = themeName; }

        public String getPrimaryColor() { return primaryColor; }
        public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

        public String getSecondaryColor() { return secondaryColor; }
        public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

        public String getAccentColor() { return accentColor; }
        public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
    }

