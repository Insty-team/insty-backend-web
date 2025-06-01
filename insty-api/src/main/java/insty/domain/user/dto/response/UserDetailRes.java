package insty.domain.user.dto.response;

import insty.model.user.User;
import insty.model.user.UserType;
import java.time.Instant;

public record UserDetailRes(
        Long id,
        String email,
        String nickname,
        boolean isEmailAgreed,
        String thumbnailUrl,
        UserType userType,
        Instant createdAt
) {
    public static UserDetailRes from(User user) {
        return new UserDetailRes(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.isEmailAgreed(),
                null,       // TODO 프로필 이미지 생성 예정
                user.getUserType(),
                user.getCreatedAt()
        );
    }

}
