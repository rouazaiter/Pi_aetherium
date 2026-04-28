package tn.esprit.aetherium.skillhubbackend.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Comment;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Post;
import tn.esprit.aetherium.skillhubbackend.entities.user.User;
import tn.esprit.aetherium.skillhubbackend.repositories.blog.CommentRepository;
import tn.esprit.aetherium.skillhubbackend.repositories.blog.PostRepository;
import tn.esprit.aetherium.skillhubbackend.repositories.user.UserRepository;
import tn.esprit.aetherium.skillhubbackend.services.interfaces.ICommentService;

import java.util.List;

import tn.esprit.aetherium.skillhubbackend.config.ModerationException;
import tn.esprit.aetherium.skillhubbackend.services.implementations.ContentModerationServiceImpl;
import tn.esprit.aetherium.skillhubbackend.services.implementations.SentimentAnalysisServiceImpl;
import tn.esprit.aetherium.skillhubbackend.services.implementations.ModerationViolationServiceImpl;

@Service
@Transactional
public class CommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ContentModerationServiceImpl moderation;
    private final SentimentAnalysisServiceImpl sentiment;
    private final ModerationViolationServiceImpl violationService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              UserRepository userRepository,
                              ContentModerationServiceImpl moderation,
                              SentimentAnalysisServiceImpl sentiment,
                              ModerationViolationServiceImpl violationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.moderation = moderation;
        this.sentiment = sentiment;
        this.violationService = violationService;
    }

    @Override
    public Comment addComment(Long postId, Long authorId, String content) {
        ContentModerationServiceImpl.ModerationResult result = moderation.check(content);
        if (result.flagged()) {
            violationService.recordViolation(authorId, result.detectedWords());
            throw new ModerationException(result.detectedWords());
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorId));
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setSentiment(sentiment.analyze(content).sentiment().name());
        return commentRepository.save(comment);
    }

    @Override
    public Comment updateComment(Long commentId, String content, Long requesterId) {
        ContentModerationServiceImpl.ModerationResult result = moderation.check(content);
        if (result.flagged()) {
            violationService.recordViolation(requesterId, result.detectedWords());
            throw new ModerationException(result.detectedWords());
        }
        Comment comment = findComment(commentId);
        assertOwner(comment.getAuthor().getId(), requesterId);
        comment.setContent(content);
        comment.setSentiment(sentiment.analyze(content).sentiment().name());
        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long commentId, Long requesterId) {
        Comment comment = findComment(commentId);
        assertOwner(comment.getAuthor().getId(), requesterId);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    private Comment findComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));
    }

    private void assertOwner(Long ownerId, Long requesterId) {
        if (!ownerId.equals(requesterId)) {
            throw new RuntimeException("Access denied: requester is not the owner");
        }
    }
}
