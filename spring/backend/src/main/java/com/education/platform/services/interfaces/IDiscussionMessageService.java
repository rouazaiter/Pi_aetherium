package com.education.platform.services.interfaces;

import com.education.platform.entities.blog.DiscussionMessage;

import java.util.List;

public interface IDiscussionMessageService {

    DiscussionMessage sendMessage(Long discussionId, Long senderId, String content, Long parentId);

    DiscussionMessage updateMessage(Long messageId, String content, Long requesterId);

    void deleteMessage(Long messageId, Long requesterId);

    List<DiscussionMessage> getTopLevelMessages(Long discussionId);

    List<DiscussionMessage> getReplies(Long parentMessageId);
}
