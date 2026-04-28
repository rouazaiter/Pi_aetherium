package tn.esprit.aetherium.skillhubbackend.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Discussion;
import tn.esprit.aetherium.skillhubbackend.entities.blog.DiscussionStatus;
import tn.esprit.aetherium.skillhubbackend.entities.user.User;
import tn.esprit.aetherium.skillhubbackend.repositories.blog.DiscussionRepository;
import tn.esprit.aetherium.skillhubbackend.repositories.user.UserRepository;
import tn.esprit.aetherium.skillhubbackend.services.interfaces.IDiscussionService;

import java.util.List;

@Service
@Transactional
public class DiscussionServiceImpl implements IDiscussionService {

    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;

    public DiscussionServiceImpl(DiscussionRepository discussionRepository, UserRepository userRepository) {
        this.discussionRepository = discussionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Discussion createDiscussion(String theme, Long creatorId) {
        User creator = findUser(creatorId);
        Discussion discussion = new Discussion();
        discussion.setTheme(theme);
        discussion.setCreator(creator);
        discussion.getParticipants().add(creator);
        return discussionRepository.save(discussion);
    }

    @Override
    public Discussion updateDiscussion(Long discussionId, String theme, Long requesterId) {
        Discussion discussion = findDiscussion(discussionId);
        assertOwner(discussion.getCreator().getId(), requesterId);
        discussion.setTheme(theme);
        return discussionRepository.save(discussion);
    }

    @Override
    public void deleteDiscussion(Long discussionId, Long requesterId) {
        Discussion discussion = findDiscussion(discussionId);
        assertOwner(discussion.getCreator().getId(), requesterId);
        discussionRepository.delete(discussion);
    }

    @Override
    @Transactional(readOnly = true)
    public Discussion getDiscussionById(Long discussionId) {
        return findDiscussion(discussionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Discussion> getDiscussionsByUser(Long userId) {
        return discussionRepository.findByParticipantId(userId);
    }

    @Override
    public void addParticipant(Long discussionId, Long userId, Long requesterId) {
        Discussion discussion = findDiscussion(discussionId);
        assertOwner(discussion.getCreator().getId(), requesterId);
        User user = findUser(userId);
        discussion.getParticipants().add(user);
        discussionRepository.save(discussion);
    }

    @Override
    public void removeParticipant(Long discussionId, Long userId, Long requesterId) {
        Discussion discussion = findDiscussion(discussionId);
        assertOwner(discussion.getCreator().getId(), requesterId);
        discussion.getParticipants().removeIf(u -> u.getId().equals(userId));
        discussionRepository.save(discussion);
    }

    private Discussion findDiscussion(Long id) {
        return discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discussion not found: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private void assertOwner(Long ownerId, Long requesterId) {
        if (!ownerId.equals(requesterId)) {
            throw new RuntimeException("Access denied: requester is not the owner");
        }
    }
}
