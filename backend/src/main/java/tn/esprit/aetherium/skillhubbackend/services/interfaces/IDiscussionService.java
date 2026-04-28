package tn.esprit.aetherium.skillhubbackend.services.interfaces;

import tn.esprit.aetherium.skillhubbackend.entities.blog.Discussion;

import java.util.List;

public interface IDiscussionService {

    Discussion createDiscussion(String theme, Long creatorId);

    Discussion updateDiscussion(Long discussionId, String theme, Long requesterId);

    void deleteDiscussion(Long discussionId, Long requesterId);

    Discussion getDiscussionById(Long discussionId);

    List<Discussion> getDiscussionsByUser(Long userId);

    void addParticipant(Long discussionId, Long userId, Long requesterId);

    void removeParticipant(Long discussionId, Long userId, Long requesterId);
}
