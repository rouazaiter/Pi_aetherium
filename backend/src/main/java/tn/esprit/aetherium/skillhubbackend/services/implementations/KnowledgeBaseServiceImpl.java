package tn.esprit.aetherium.skillhubbackend.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.aetherium.skillhubbackend.entities.blog.*;
import tn.esprit.aetherium.skillhubbackend.repositories.blog.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class KnowledgeBaseServiceImpl {

    private final KnowledgeBaseRepository kbRepository;
    private final DiscussionRepository discussionRepository;
    private final DiscussionMessageRepository messageRepository;

    // Common stop words to exclude from auto-tags
    private static final Set<String> STOP_WORDS = Set.of(
        "the","a","an","is","it","in","on","at","to","for","of","and","or","but",
        "how","what","why","when","where","who","which","this","that","with","from",
        "le","la","les","un","une","des","est","en","et","ou","pour","dans","sur"
    );

    public KnowledgeBaseServiceImpl(KnowledgeBaseRepository kbRepository,
                                    DiscussionRepository discussionRepository,
                                    DiscussionMessageRepository messageRepository) {
        this.kbRepository = kbRepository;
        this.discussionRepository = discussionRepository;
        this.messageRepository = messageRepository;
    }

    /** Mark a discussion as solved and auto-create a KB article */
    public KnowledgeBaseArticle markSolved(Long discussionId, Long acceptedMessageId, Long requesterId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Discussion not found: " + discussionId));

        if (!discussion.getCreator().getId().equals(requesterId)) {
            throw new RuntimeException("Only the discussion creator can mark it as solved");
        }

        DiscussionMessage acceptedMessage = messageRepository.findById(acceptedMessageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + acceptedMessageId));

        // Mark discussion as archived/solved
        discussion.setStatus(DiscussionStatus.ARCHIVED);
        discussion.setSolvedMessageId(acceptedMessageId);
        discussionRepository.save(discussion);

        // Auto-generate tags from the discussion theme
        String tags = extractTags(discussion.getTheme());

        // Build the KB article
        KnowledgeBaseArticle article = new KnowledgeBaseArticle();
        article.setQuestion(discussion.getTheme());
        article.setAnswer(acceptedMessage.getContent());
        article.setTags(tags);
        article.setSourceDiscussion(discussion);
        article.setAuthor(discussion.getCreator());

        return kbRepository.save(article);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseArticle> getAll() {
        return kbRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseArticle> search(String query) {
        return kbRepository.search(query);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseArticle> getPopular() {
        return kbRepository.findAllByOrderByViewsDesc();
    }

    @Transactional(readOnly = true)
    public KnowledgeBaseArticle getById(Long id) {
        KnowledgeBaseArticle article = kbRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found: " + id));
        article.setViews(article.getViews() + 1);
        kbRepository.save(article);
        return article;
    }

    /** Extract meaningful keywords from text as comma-separated tags */
    private String extractTags(String text) {
        return Arrays.stream(text.toLowerCase().split("[\\s,\\.!?;:'\"-]+"))
                .filter(w -> w.length() > 3)
                .filter(w -> !STOP_WORDS.contains(w))
                .distinct()
                .limit(5)
                .collect(Collectors.joining(","));
    }
}
