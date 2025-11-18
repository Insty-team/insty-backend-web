package insty.domain.notification.controller;

import insty.domain.notification.service.UserNotificationSettingMigrationService;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "알림 설정 마이그레이션 API (임시)")
@RestController
@RequestMapping("/api/v1/notification/migration")
@RequiredArgsConstructor
public class NotificationSettingMigrationController {

    private final UserNotificationSettingMigrationService migrationService;

    @Operation(
            summary = "모든 사용자 알림 설정 마이그레이션",
            description = "알림 설정이 없는 모든 기존 사용자에게 기본 알림 설정을 초기화합니다. (Admin용 임시 API)"
    )
    @PostMapping("/all")
    public SuccessRes<MigrationResultDto> migrateAllUsers() {
        log.info("알림 설정 마이그레이션 API 호출");

        int migratedCount = migrationService.migrateAllUsersWithoutSettings();

        MigrationResultDto result = new MigrationResultDto(
                migratedCount,
                "마이그레이션이 완료되었습니다."
        );

        return SuccessRes.of(result);
    }

    @Operation(
            summary = "특정 사용자 알림 설정 리셋",
            description = "특정 사용자의 알림 설정을 강제로 초기화합니다. (Admin용 임시 API)"
    )
    @PostMapping("/user/{userId}")
    public SuccessRes<String> resetUserSettings(@PathVariable Long userId) {
        log.info("사용자 알림 설정 리셋 API 호출 - userId: {}", userId);

        migrationService.resetUserSettings(userId);

        return SuccessRes.of("사용자 알림 설정이 리셋되었습니다.");
    }

    public record MigrationResultDto(
            int migratedCount,
            String message
    ) {
    }
}
