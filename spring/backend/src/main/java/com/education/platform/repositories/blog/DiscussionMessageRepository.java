package com.education.platform.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import com.education.platform.entities.blog.DiscussionMessage;

import java.util.List;

public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, Long> {

    // Top-level messages (no parent)
    List<DiscussionMessage> findByDiscussionIdAndParentIsNull(Long discussionId);

    // Replies to a specific message
    List<DiscussionMessage> findByParentId(Long parentId);
}
