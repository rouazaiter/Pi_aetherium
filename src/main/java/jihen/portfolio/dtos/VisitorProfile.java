package jihen.portfolio.dtos;

public class VisitorProfile {

        private String type; // RECRUITER, CLIENT, FREELANCE, DEFAULT
        private String source; // LINKEDIN, GITHUB, GOOGLE, DIRECT
        private String priority; // EXPERIENCE, PROJECTS, AVAILABILITY, SKILLS

        public VisitorProfile() {
            this.type = "DEFAULT";
            this.source = "DIRECT";
            this.priority = "PROJECTS";
        }

        // Getters et Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

