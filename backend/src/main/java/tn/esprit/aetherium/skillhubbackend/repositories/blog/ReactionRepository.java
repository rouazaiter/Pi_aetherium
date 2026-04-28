package tn.esprit.aetherium.skillhubbackend.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Reaction;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndMessageId(Long userId, Long messageId);

    List<Reaction> findByMessageId(Long messageId);
}
