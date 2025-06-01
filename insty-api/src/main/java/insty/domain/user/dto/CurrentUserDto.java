package insty.domain.user.dto;

import insty.global.security.CustomUserDetails;

public record CurrentUserDto (
        Long id,
        String email,
        String nickname
) {
    public static CurrentUserDto from(CustomUserDetails customUserDetails) {
        return new CurrentUserDto(
                customUserDetails.getUserId(),
                customUserDetails.getUsername(),
                customUserDetails.getNickname());
    }
}
