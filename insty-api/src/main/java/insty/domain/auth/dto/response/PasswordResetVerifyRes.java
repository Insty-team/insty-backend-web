package insty.domain.auth.dto.response;

import java.time.LocalDateTime;

public record PasswordResetVerifyRes(
        String email,
        boolean verified,
        LocalDateTime expiredAt
) {
    public static PasswordResetVerifyRes from(String email, boolean verified , LocalDateTime expiredAt){
        return new PasswordResetVerifyRes(email,verified,expiredAt);
    }
}