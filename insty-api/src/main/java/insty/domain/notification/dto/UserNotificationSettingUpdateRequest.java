package insty.domain.notification.dto;

import insty.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 알림 설정 변경 요청 DTO
 */
@Schema(description = "알림 설정 변경 요청")
public record UserNotificationSettingUpdateRequest(

        @Schema(description = "알림 타입", example = "NEW_COMMUNITY_QUESTION")
        @NotNull(message = "알림 타입은 필수입니다")
        NotificationType notificationType,

        @Schema(description = "인앱 알림 활성화 여부", example = "true")
        @NotNull(message = "인앱 알림 설정은 필수입니다")
        Boolean inAppEnabled,

        @Schema(description = "이메일 알림 활성화 여부", example = "false")
        @NotNull(message = "이메일 알림 설정은 필수입니다")
        Boolean emailEnabled
) {
}
