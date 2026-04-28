package tn.esprit.aetherium.skillhubbackend.services.interfaces;

import tn.esprit.aetherium.skillhubbackend.entities.blog.Reaction;
import tn.esprit.aetherium.skillhubbackend.entities.blog.ReactionType;

import java.util.List;

public interface IReactionService {

    Reaction reactToMessage(Long messageId, Long userId, ReactionType type);

    void removeReaction(Long messageId, Long userId);

    List<Reaction> getReactionsByMessage(Long messageId);
}
