package com.education.platform.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.education.platform.entities.blog.DiscussionMessage;
import com.education.platform.entities.blog.Reaction;
import com.education.platform.entities.blog.ReactionType;
import com.education.platform.entities.user.User;
import com.education.platform.repositories.blog.DiscussionMessageRepository;
import com.education.platform.repositories.blog.ReactionRepository;
import com.education.platform.repositories.user.UserRepository;
import com.education.platform.services.interfaces.IReactionService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReactionServiceImpl implements IReactionService {

    private final ReactionRepository reactionRepository;
    private final DiscussionMessageRepository messageRepository;
    private final UserRepository userRepository;

    public ReactionServiceImpl(ReactionRepository reactionRepository,
                               DiscussionMessageRepository messageRepository,
                               UserRepository userRepository) {
        this.reactionRepository = reactionRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Reaction reactToMessage(Long messageId, Long userId, ReactionType type) {
        DiscussionMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Optional<Reaction> existing = reactionRepository.findByUserIdAndMessageId(userId, messageId);
        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            reaction.setType(type);
            return reactionRepository.save(reaction);
        }

        Reaction reaction = new Reaction();
        reaction.setUser(user);
        reaction.setMessage(message);
        reaction.setType(type);
        return reactionRepository.save(reaction);
    }

    @Override
    public void removeReaction(Long messageId, Long userId) {
        Reaction reaction = reactionRepository.findByUserIdAndMessageId(userId, messageId)
                .orElseThrow(() -> new RuntimeException("Reaction not found"));
        reactionRepository.delete(reaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reaction> getReactionsByMessage(Long messageId) {
        return reactionRepository.findByMessageId(messageId);
    }
}
