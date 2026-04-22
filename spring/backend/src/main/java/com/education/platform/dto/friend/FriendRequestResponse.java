package com.education.platform.dto.friend;

import com.education.platform.entities.FriendRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestResponse {

    private Long id;
    private FriendResponse sender;
    private FriendResponse receiver;
    private FriendRequestStatus status;
    private Instant createdAt;
}
