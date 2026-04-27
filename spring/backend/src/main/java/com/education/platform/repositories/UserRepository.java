package com.education.platform.repositories;

import com.education.platform.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    List<User> findByUsernameContainingIgnoreCaseOrderByUsernameAsc(String username, Pageable pageable);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByFacebookId(String facebookId);

    @Query(
            """
            select (count(u) > 0)
            from User u
            join u.friends f
            where u.id = :userId and f.id = :friendId
            """
    )
    boolean areFriends(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
