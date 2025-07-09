package insty.domain.user.dto.response;

import insty.model.user.SocialType;
import insty.model.user.User;
import insty.model.user.UserType;
import java.time.Instant;

public record UserDetailRes(
        Long id,
        String email,
        String nickname,
        boolean isEmailAgreed,
        String thumbnailUrl,
        String introduce,
        SocialType socialType,
        UserType userType,
        Instant createdAt
) {
    public static UserDetailRes from(User user, String thumbnailUrl) {
        return new UserDetailRes(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.isEmailAgreed(),
                thumbnailUrl,
                user.getIntroduce(),
                user.getSocialType(),
                user.getUserType(),
                user.getCreatedAt()
        );
    }

}
