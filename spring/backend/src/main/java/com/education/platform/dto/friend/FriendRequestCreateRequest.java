package com.education.platform.dto.friend;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestCreateRequest {

    @NotNull(message = "Identifiant utilisateur requis")
    private Long userId;
}
