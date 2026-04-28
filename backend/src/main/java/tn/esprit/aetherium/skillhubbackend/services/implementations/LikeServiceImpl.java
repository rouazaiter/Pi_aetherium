package tn.esprit.aetherium.skillhubbackend.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Comment;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Like;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Post;
import tn.esprit.aetherium.skillhubbackend.entities.user.User;
import tn.esprit.aetherium.skillhubbackend.repositories.blog.CommentRepository;
import tn.esprit.aetherium.skillhubbackend.repositories.blog.LikeRepository;
import tn.esprit.aetherium.skillhubbackend.repositories.blog.PostRepository;
import tn.esprit.aetherium.skillhubbackend.repositories.user.UserRepository;
import tn.esprit.aetherium.skillhubbackend.services.interfaces.ILikeService;

@Service
@Transactional
public class LikeServiceImpl implements ILikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public LikeServiceImpl(LikeRepository likeRepository,
                           PostRepository postRepository,
                           CommentRepository commentRepository,
                           UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void likePost(Long postId, Long userId) {
        if (likeRepository.findByUserIdAndPostId(userId, postId).isPresent()) {
            throw new RuntimeException("User already liked this post");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);
    }

    @Override
    public void unlikePost(Long postId, Long userId) {
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        likeRepository.delete(like);
    }

    @Override
    public void likeComment(Long commentId, Long userId) {
        if (likeRepository.findByUserIdAndCommentId(userId, commentId).isPresent()) {
            throw new RuntimeException("User already liked this comment");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Like like = new Like();
        like.setUser(user);
        like.setComment(comment);
        likeRepository.save(like);
    }

    @Override
    public void unlikeComment(Long commentId, Long userId) {
        Like like = likeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        likeRepository.delete(like);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPostLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCommentLikes(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }
}
