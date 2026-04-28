package tn.esprit.aetherium.skillhubbackend.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.aetherium.skillhubbackend.entities.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
