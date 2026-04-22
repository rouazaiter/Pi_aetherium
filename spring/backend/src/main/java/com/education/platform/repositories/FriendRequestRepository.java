package com.education.platform.repositories;

import com.education.platform.entities.FriendRequest;
import com.education.platform.entities.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiver_IdAndStatusOrderByCreatedAtDesc(Long receiverId, FriendRequestStatus status);

    Optional<FriendRequest> findByIdAndReceiver_Id(Long requestId, Long receiverId);

    @Query("""
            select (count(fr) > 0) from FriendRequest fr
            where fr.status = com.education.platform.entities.FriendRequestStatus.PENDING
              and ((fr.sender.id = :userA and fr.receiver.id = :userB)
               or  (fr.sender.id = :userB and fr.receiver.id = :userA))
            """)
    boolean existsPendingBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("""
            select fr from FriendRequest fr
            where fr.status = com.education.platform.entities.FriendRequestStatus.PENDING
              and fr.sender.id = :senderId and fr.receiver.id = :receiverId
            """)
    Optional<FriendRequest> findPendingBySenderAndReceiver(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
