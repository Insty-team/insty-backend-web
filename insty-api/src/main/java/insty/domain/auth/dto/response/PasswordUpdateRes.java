package insty.domain.auth.dto.response;

import java.time.LocalDateTime;

public record PasswordUpdateRes(
        String email,
        boolean passwordUpdated,
        LocalDateTime passwordUpdatedAt
) {
    public static PasswordUpdateRes from(String email, boolean passwordUpdated , LocalDateTime passwordUpdatedAt ){
        return new PasswordUpdateRes(email,passwordUpdated,passwordUpdatedAt);
    }
}