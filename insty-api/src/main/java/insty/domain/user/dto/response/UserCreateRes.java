package insty.domain.user.dto.response;

import insty.model.user.UserType;

public record UserCreateRes (
        Long id,
        String nickname,
        String email,
        UserType userType
) {
    public static UserCreateRes from(Long id, String email, String nickname, UserType userType) {
        return new UserCreateRes(id, email, nickname, userType);
    }
}
