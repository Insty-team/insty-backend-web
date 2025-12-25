package insty.domain.courseqna.dto;

import insty.domain.common.dto.UserInfo;
import insty.model.user.User;
import insty.model.user.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

public record CourseUserRes(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 닉네임", example = "김철수")
        String nickname,

        @Schema(description = "사용자 타입", example = "LEARNER")
        UserType userType
) {
    public static CourseUserRes from(User user) {
        return new CourseUserRes(user.getId(), user.getNickname(), user.getUserType());
    }

    public static CourseUserRes from(UserInfo userInfo) {
        return new CourseUserRes(userInfo.id(), userInfo.nickname(), userInfo.userType());
    }
}
