package com.education.platform.services.interfaces;

import com.education.platform.dto.friend.FriendResponse;
import com.education.platform.dto.friend.FriendRequestResponse;
import com.education.platform.dto.friend.FriendSearchResponse;
import com.education.platform.dto.friend.FriendProfileResponse;
import com.education.platform.entities.User;

import java.util.List;

public interface FriendService {

    List<FriendResponse> listFriends(User current);

    List<FriendSearchResponse> searchUsers(User current, String query);

    List<FriendSearchResponse> discoverUsers(User current);

    FriendRequestResponse sendFriendRequest(User current, Long userId);

    List<FriendRequestResponse> listIncomingRequests(User current);

    FriendResponse acceptFriendRequest(User current, Long requestId);

    FriendProfileResponse getFriendProfile(User current, Long friendId);

    void declineFriendRequest(User current, Long requestId);

    void removeFriend(User current, Long friendId);
}
