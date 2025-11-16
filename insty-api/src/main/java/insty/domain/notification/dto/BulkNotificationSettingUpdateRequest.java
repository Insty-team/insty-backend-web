package insty.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 모든 알림 일괄 설정 요청 DTO
 */
@Schema(description = "모든 알림 일괄 설정 요청")
public record BulkNotificationSettingUpdateRequest(

        @Schema(description = "모든 알림 활성화 여부", example = "true")
        @NotNull(message = "활성화 여부는 필수입니다")
        Boolean enableAll
) {
}
