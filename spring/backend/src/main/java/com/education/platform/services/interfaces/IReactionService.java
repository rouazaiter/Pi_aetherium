package com.education.platform.services.interfaces;

import com.education.platform.entities.blog.Reaction;
import com.education.platform.entities.blog.ReactionType;

import java.util.List;

public interface IReactionService {

    Reaction reactToMessage(Long messageId, Long userId, ReactionType type);

    void removeReaction(Long messageId, Long userId);

    List<Reaction> getReactionsByMessage(Long messageId);
}
