package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.friend.FriendRequestResponse;
import com.education.platform.dto.friend.FriendSearchResponse;
import com.education.platform.dto.friend.FriendResponse;
import com.education.platform.entities.FriendRequest;
import com.education.platform.entities.FriendRequestStatus;
import com.education.platform.entities.User;
import com.education.platform.repositories.FriendRequestRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.FriendService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;

    public FriendServiceImpl(UserRepository userRepository, FriendRequestRepository friendRequestRepository) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
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
    public List<FriendSearchResponse> searchUsers(User current, String query) {
        String trimmed = query != null ? query.trim() : "";
        if (trimmed.length() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tapez au moins 2 caracteres pour rechercher");
        }
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrderByUsernameAsc(trimmed, PageRequest.of(0, 20));
        return users.stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .map(u -> FriendSearchResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .firstName(u.getProfile() != null ? u.getProfile().getFirstName() : null)
                        .lastName(u.getProfile() != null ? u.getProfile().getLastName() : null)
                        .relation(resolveRelation(me, u))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public FriendRequestResponse sendFriendRequest(User current, Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Utilisateur cible invalide");
        }
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aucun utilisateur avec ce nom"));

        if (me.getId().equals(receiver.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas vous envoyer une demande");
        }
        if (isFriend(me, receiver.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Cet utilisateur est deja votre ami");
        }
        if (friendRequestRepository.existsPendingBetweenUsers(me.getId(), receiver.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Une demande d'ami est deja en attente");
        }

        FriendRequest request = FriendRequest.builder()
                .sender(me)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        friendRequestRepository.save(request);
        return toRequestResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestResponse> listIncomingRequests(User current) {
        return friendRequestRepository.findByReceiver_IdAndStatusOrderByCreatedAtDesc(current.getId(), FriendRequestStatus.PENDING).stream()
                .map(FriendServiceImpl::toRequestResponse)
                .toList();
    }

    @Override
    @Transactional
    public FriendResponse acceptFriendRequest(User current, Long requestId) {
        FriendRequest request = findIncomingPendingRequest(current, requestId);
        User me = request.getReceiver();
        User sender = request.getSender();

        if (!isFriend(me, sender.getId())) {
            me.getFriends().add(sender);
            sender.getFriends().add(me);
            userRepository.save(me);
            userRepository.save(sender);
        }
        request.setStatus(FriendRequestStatus.ACCEPTED);
        return toResponse(sender);
    }

    @Override
    @Transactional
    public void declineFriendRequest(User current, Long requestId) {
        FriendRequest request = findIncomingPendingRequest(current, requestId);
        request.setStatus(FriendRequestStatus.DECLINED);
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

    private FriendRequest findIncomingPendingRequest(User current, Long requestId) {
        if (requestId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Identifiant de demande invalide");
        }
        FriendRequest request = friendRequestRepository.findByIdAndReceiver_Id(requestId, current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette demande n'est plus en attente");
        }
        return request;
    }

    private String resolveRelation(User me, User candidate) {
        if (isFriend(me, candidate.getId())) {
            return "FRIEND";
        }
        if (friendRequestRepository.findPendingBySenderAndReceiver(me.getId(), candidate.getId()).isPresent()) {
            return "REQUEST_SENT";
        }
        if (friendRequestRepository.findPendingBySenderAndReceiver(candidate.getId(), me.getId()).isPresent()) {
            return "REQUEST_RECEIVED";
        }
        return "NONE";
    }

    private boolean isFriend(User me, Long otherUserId) {
        return me.getFriends().stream().anyMatch(u -> u.getId().equals(otherUserId));
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

    private static FriendRequestResponse toRequestResponse(FriendRequest request) {
        return FriendRequestResponse.builder()
                .id(request.getId())
                .sender(toResponse(request.getSender()))
                .receiver(toResponse(request.getReceiver()))
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
