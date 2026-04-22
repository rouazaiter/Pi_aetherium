package com.education.platform.controllers;

import com.education.platform.dto.friend.FriendRequestCreateRequest;
import com.education.platform.dto.friend.FriendRequestResponse;
import com.education.platform.dto.friend.FriendSearchResponse;
import com.education.platform.dto.friend.FriendResponse;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.FriendService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final CurrentUserService currentUserService;
    private final FriendService friendService;

    public FriendController(CurrentUserService currentUserService, FriendService friendService) {
        this.currentUserService = currentUserService;
        this.friendService = friendService;
    }

    @GetMapping
    public List<FriendResponse> list() {
        return friendService.listFriends(currentUserService.getCurrentUser());
    }

    @GetMapping("/search")
    public List<FriendSearchResponse> search(@RequestParam("query") String query) {
        return friendService.searchUsers(currentUserService.getCurrentUser(), query);
    }

    @GetMapping("/requests/incoming")
    public List<FriendRequestResponse> incomingRequests() {
        return friendService.listIncomingRequests(currentUserService.getCurrentUser());
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequestResponse sendRequest(@Valid @RequestBody FriendRequestCreateRequest request) {
        return friendService.sendFriendRequest(currentUserService.getCurrentUser(), request.getUserId());
    }

    @PostMapping("/requests/{requestId}/accept")
    public FriendResponse acceptRequest(@PathVariable Long requestId) {
        return friendService.acceptFriendRequest(currentUserService.getCurrentUser(), requestId);
    }

    @PostMapping("/requests/{requestId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineRequest(@PathVariable Long requestId) {
        friendService.declineFriendRequest(currentUserService.getCurrentUser(), requestId);
    }

    @DeleteMapping("/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long friendId) {
        friendService.removeFriend(currentUserService.getCurrentUser(), friendId);
    }
}
