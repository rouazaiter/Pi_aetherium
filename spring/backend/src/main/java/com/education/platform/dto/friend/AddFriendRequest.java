package com.education.platform.dto.friend;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFriendRequest {

    @NotBlank(message = "Nom d'utilisateur de l'ami requis")
    private String username;
}
