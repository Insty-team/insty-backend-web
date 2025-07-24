package insty.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailSendReq(
    @NotBlank
    @Email
    String email
) {
}
