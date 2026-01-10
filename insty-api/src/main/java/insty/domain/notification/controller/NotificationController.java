package insty.domain.notification.controller;

import insty.domain.notification.dto.response.NotificationRes;
import insty.domain.notification.service.NotificationService;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림 API")
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "사용자 알림 조회", description = "로그인한 사용자의 알림 목록을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_SEARCH)
    @GetMapping
    public SuccessRes<List<NotificationRes>> getUserNotifications(
            @CurrentUser Long userId
    ) {
        return SuccessRes.of(notificationService.getUserNotifications(userId));
    }

    @Operation(summary = "알림 읽음 처리 및 이동", description = "알림을 읽음 처리하고, 해당 알림의 리다이렉트 URL을 반환한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_READ)
    @PostMapping("/{notificationId}/read")
    public SuccessRes<String> markNotificationAsRead(
            @CurrentUser Long userId,
            @PathVariable Long notificationId
    ) {
        String redirectUrl = notificationService.markAsReadAndRedirect(notificationId, userId);
        return SuccessRes.of(redirectUrl);
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_READ_ALL)
    @PostMapping("/read-all")
    public SuccessRes<String> markAllNotificationsAsRead(
            @CurrentUser Long userId
    ) {
        notificationService.markAllAsRead(userId);
        return SuccessRes.of("모든 알림을 읽음 처리했습니다.");
    }

    @Operation(summary = "모든 알림 취소", description = "사용자의 모든 알림을 취소한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_CANCEL_ALL)
    @DeleteMapping
    public SuccessRes<String> cancelAllNotifications(
            @CurrentUser Long userId
    ) {
        notificationService.cancelAll(userId);
        return SuccessRes.of("모든 알림을 취소했습니다.");
    }
}
