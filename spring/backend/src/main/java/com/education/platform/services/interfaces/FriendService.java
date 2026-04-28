package com.education.platform.services.interfaces;

import com.education.platform.dto.friend.FriendResponse;
import com.education.platform.entities.User;

import java.util.List;

public interface FriendService {

    List<FriendResponse> listFriends(User current);

    FriendResponse addFriend(User current, String friendUsername);

    void removeFriend(User current, Long friendId);
}
