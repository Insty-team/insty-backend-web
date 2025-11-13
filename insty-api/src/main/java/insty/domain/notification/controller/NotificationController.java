package insty.domain.notification.controller;


import insty.domain.notification.dto.NotificationResponse;
import insty.domain.notification.service.NotificationService;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "알림 API")
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "사용자 알림 조회", description = "로그인한 사용자의 알림 목록을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_SEARCH)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping
    public SuccessRes<List<NotificationResponse>> getUserNotifications(
            @CurrentUser Long userId
    ) {
        return SuccessRes.of(notificationService.getUserNotifications(userId));
    }

    @Operation(summary = "알림 읽음 처리 및 이동", description = "알림을 읽음 처리하고, 해당 알림의 리다이렉트 URL을 반환한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.NOTIFICATION_READ)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PostMapping("/{notificationId}/read")
    public SuccessRes<String> markNotificationAsRead(
            @CurrentUser Long userId,
            @PathVariable Long notificationId
    ) {
        String redirectUrl = notificationService.markAsReadAndRedirect(notificationId, userId);
        return SuccessRes.of(redirectUrl);
    }
}
