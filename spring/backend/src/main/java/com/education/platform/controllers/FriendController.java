package com.education.platform.controllers;

import com.education.platform.dto.friend.AddFriendRequest;
import com.education.platform.dto.friend.FriendResponse;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.FriendService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FriendResponse add(@Valid @RequestBody AddFriendRequest request) {
        return friendService.addFriend(currentUserService.getCurrentUser(), request.getUsername());
    }

    @DeleteMapping("/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long friendId) {
        friendService.removeFriend(currentUserService.getCurrentUser(), friendId);
    }
}
