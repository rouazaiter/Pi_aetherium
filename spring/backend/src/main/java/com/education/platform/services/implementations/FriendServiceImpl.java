package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.friend.FriendResponse;
import com.education.platform.entities.User;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;

    public FriendServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> listFriends(User current) {
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return me.getFriends().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(FriendServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FriendResponse addFriend(User current, String friendUsername) {
        String trimmed = friendUsername != null ? friendUsername.trim() : "";
        if (trimmed.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nom d'utilisateur invalide");
        }
        if (current.getUsername().equalsIgnoreCase(trimmed)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas vous ajouter vous-même comme ami");
        }
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        User friend = userRepository.findByUsername(trimmed)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aucun utilisateur avec ce nom"));

        if (me.getFriends().stream().anyMatch(u -> u.getId().equals(friend.getId()))) {
            throw new ApiException(HttpStatus.CONFLICT, "Cet utilisateur est déjà dans votre liste d'amis");
        }
        me.getFriends().add(friend);
        friend.getFriends().add(me);
        userRepository.save(me);
        userRepository.save(friend);
        return toResponse(friend);
    }

    @Override
    @Transactional
    public void removeFriend(User current, Long friendId) {
        if (friendId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Identifiant invalide");
        }
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ami introuvable"));
        boolean linked = me.getFriends().removeIf(u -> u.getId().equals(friendId));
        if (!linked) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Cet utilisateur n'est pas dans votre liste d'amis");
        }
        friend.getFriends().removeIf(u -> u.getId().equals(me.getId()));
        userRepository.save(me);
        userRepository.save(friend);
    }

    private static FriendResponse toResponse(User u) {
        var p = u.getProfile();
        return FriendResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .firstName(p != null ? p.getFirstName() : null)
                .lastName(p != null ? p.getLastName() : null)
                .build();
    }
}
