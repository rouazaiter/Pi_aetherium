package com.education.platform.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import com.education.platform.entities.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
