package insty.domain.common;

import insty.model.user.User;

public record CreatorInfo(
        Long id,
        String nickname
) {

    public static CreatorInfo from(User user) {
        return new CreatorInfo(user.getId(), user.getNickname());
    }
}
