package insty.domain.auth.dto.response;

import java.time.LocalDateTime;

public record PasswordResetMailRes(
        String email,
        LocalDateTime expiredAt
) {
    public static PasswordResetMailRes from(String email, LocalDateTime expiredAt){
        return new PasswordResetMailRes(email,expiredAt);
    }
}