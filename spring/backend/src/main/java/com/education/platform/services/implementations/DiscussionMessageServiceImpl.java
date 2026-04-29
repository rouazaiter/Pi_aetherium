package com.education.platform.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.education.platform.entities.blog.Discussion;
import com.education.platform.entities.blog.DiscussionMessage;
import com.education.platform.entities.user.User;
import com.education.platform.repositories.blog.DiscussionMessageRepository;
import com.education.platform.repositories.blog.DiscussionRepository;
import com.education.platform.repositories.user.UserRepository;
import com.education.platform.services.interfaces.IDiscussionMessageService;

import java.util.List;

@Service
@Transactional
public class DiscussionMessageServiceImpl implements IDiscussionMessageService {

    private final DiscussionMessageRepository messageRepository;
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;

    public DiscussionMessageServiceImpl(DiscussionMessageRepository messageRepository,
                                        DiscussionRepository discussionRepository,
                                        UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.discussionRepository = discussionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DiscussionMessage sendMessage(Long discussionId, Long senderId, String content, Long parentId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Discussion not found: " + discussionId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found: " + senderId));

        boolean isParticipant = discussion.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(senderId));
        if (!isParticipant) {
            throw new RuntimeException("User is not a participant of this discussion");
        }

        DiscussionMessage message = new DiscussionMessage();
        message.setContent(content);
        message.setDiscussion(discussion);
        message.setSender(sender);

        if (parentId != null) {
            DiscussionMessage parent = messageRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent message not found: " + parentId));
            message.setParent(parent);
        }

        return messageRepository.save(message);
    }

    @Override
    public DiscussionMessage updateMessage(Long messageId, String content, Long requesterId) {
        DiscussionMessage message = findMessage(messageId);
        assertOwner(message.getSender().getId(), requesterId);
        message.setContent(content);
        return messageRepository.save(message);
    }

    @Override
    public void deleteMessage(Long messageId, Long requesterId) {
        DiscussionMessage message = findMessage(messageId);
        assertOwner(message.getSender().getId(), requesterId);
        messageRepository.delete(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscussionMessage> getTopLevelMessages(Long discussionId) {
        return messageRepository.findByDiscussionIdAndParentIsNull(discussionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscussionMessage> getReplies(Long parentMessageId) {
        return messageRepository.findByParentId(parentMessageId);
    }

    private DiscussionMessage findMessage(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found: " + id));
    }

    private void assertOwner(Long ownerId, Long requesterId) {
        if (!ownerId.equals(requesterId)) {
            throw new RuntimeException("Access denied: requester is not the sender");
        }
    }
}
