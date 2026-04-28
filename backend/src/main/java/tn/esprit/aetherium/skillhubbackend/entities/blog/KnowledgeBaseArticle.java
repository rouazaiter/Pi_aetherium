package tn.esprit.aetherium.skillhubbackend.entities.blog;

import jakarta.persistence.*;
import tn.esprit.aetherium.skillhubbackend.entities.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_base")
public class KnowledgeBaseArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column
    private String tags; // comma-separated

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "discussion_id")
    private Discussion sourceDiscussion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private int views = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Discussion getSourceDiscussion() { return sourceDiscussion; }
    public void setSourceDiscussion(Discussion sourceDiscussion) { this.sourceDiscussion = sourceDiscussion; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
}
