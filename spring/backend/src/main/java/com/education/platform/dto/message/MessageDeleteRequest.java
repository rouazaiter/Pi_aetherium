package com.education.platform.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDeleteRequest {

    @NotBlank
    private String scope;
}
