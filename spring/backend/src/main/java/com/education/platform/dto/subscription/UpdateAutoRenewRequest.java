package com.education.platform.dto.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAutoRenewRequest {

    @NotNull(message = "Le statut auto-renew est requis")
    private Boolean autoRenew;
}
