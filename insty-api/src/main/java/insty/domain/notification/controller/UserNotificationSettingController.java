package insty.domain.notification.controller;

import insty.domain.notification.dto.request.BulkNotificationSettingUpdateReq;
import insty.domain.notification.dto.response.UserNotificationSettingRes;
import insty.domain.notification.dto.request.UserNotificationSettingUpdateReq;
import insty.domain.notification.service.NotificationSettingsService;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "사용자 알림 설정 API")
@RestController
@RequestMapping("/api/v1/notification/settings")
@RequiredArgsConstructor
public class UserNotificationSettingController {

    private final NotificationSettingsService preferenceService;

    @Operation(
            summary = "내 알림 설정 조회",
            description = "로그인한 사용자의 모든 알림 타입별 설정을 조회합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_SETTING_SEARCH)
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public SuccessRes<UserNotificationSettingRes> getMySettings(
            @CurrentUser Long userId
    ) {
        Map<NotificationType, Map<NotificationChannel, Boolean>> settings =
                preferenceService.getOrCreateUserSettings(userId);

        UserNotificationSettingRes response = UserNotificationSettingRes.from(settings);

        return SuccessRes.of(response);
    }

    @Operation(
            summary = "특정 알림 타입 설정 변경",
            description = "특정 알림 타입에 대한 인앱/이메일 설정을 변경합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_SETTING_UPDATE)
    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public SuccessRes<String> updateSetting(
            @CurrentUser Long userId,
            @Valid @RequestBody UserNotificationSettingUpdateReq request
    ) {
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
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/bulk")
    public SuccessRes<String> toggleAllNotifications(
            @CurrentUser Long userId,
            @Valid @RequestBody BulkNotificationSettingUpdateReq request
    ) {
        preferenceService.toggleAllNotifications(userId, request.enableAll());

        return SuccessRes.of(
                request.enableAll() ? "모든 알림이 활성화되었습니다." : "모든 알림이 비활성화되었습니다."
        );
    }
}
