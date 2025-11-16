package insty.domain.notification.controller;

import insty.domain.notification.dto.BulkNotificationSettingUpdateRequest;
import insty.domain.notification.dto.UserNotificationSettingResponse;
import insty.domain.notification.dto.UserNotificationSettingUpdateRequest;
import insty.domain.notification.service.NotificationPreferenceService;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 사용자 알림 설정 Controller
 * 사용자가 자신의 알림 수신 설정을 조회하고 변경할 수 있는 API
 */
@Slf4j
@Tag(name = "사용자 알림 설정 API")
@RestController
@RequestMapping("/api/v1/notification/settings")
@RequiredArgsConstructor
public class UserNotificationSettingController {

    private final NotificationPreferenceService preferenceService;

    @Operation(
            summary = "내 알림 설정 조회",
            description = "로그인한 사용자의 모든 알림 타입별 설정을 조회합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_SETTING_SEARCH)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping
    public SuccessRes<UserNotificationSettingResponse> getMySettings(
            @CurrentUser Long userId
    ) {
        log.debug("사용자 알림 설정 조회 - userId: {}", userId);

        Map<NotificationType, Map<NotificationChannel, Boolean>> settings =
                preferenceService.getUserSettings(userId);

        UserNotificationSettingResponse response = UserNotificationSettingResponse.from(settings);

        return SuccessRes.of(response);
    }

    @Operation(
            summary = "특정 알림 타입 설정 변경",
            description = "특정 알림 타입에 대한 인앱/이메일 설정을 변경합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_SETTING_UPDATE)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PutMapping
    public SuccessRes<String> updateSetting(
            @CurrentUser Long userId,
            @Valid @RequestBody UserNotificationSettingUpdateRequest request
    ) {
        log.info("사용자 알림 설정 변경 - userId: {}, type: {}, inApp: {}, email: {}",
                userId, request.notificationType(), request.inAppEnabled(), request.emailEnabled());

        preferenceService.updateSettingsForType(
                userId,
                request.notificationType(),
                request.inAppEnabled(),
                request.emailEnabled()
        );

        return SuccessRes.of("알림 설정이 변경되었습니다.");
    }

    @Operation(
            summary = "모든 알림 일괄 설정",
            description = "모든 알림 타입의 설정을 한 번에 켜거나 끕니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_SETTING_BULK_UPDATE)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PutMapping("/bulk")
    public SuccessRes<String> toggleAllNotifications(
            @CurrentUser Long userId,
            @Valid @RequestBody BulkNotificationSettingUpdateRequest request
    ) {
        log.info("사용자 모든 알림 일괄 변경 - userId: {}, enableAll: {}", userId, request.enableAll());

        preferenceService.toggleAllNotifications(userId, request.enableAll());

        return SuccessRes.of(
                request.enableAll() ? "모든 알림이 활성화되었습니다." : "모든 알림이 비활성화되었습니다."
        );
    }
}
