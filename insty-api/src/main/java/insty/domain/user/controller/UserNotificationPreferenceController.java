package insty.domain.user.controller;

import insty.domain.user.controller.docs.UserNotificationPreferenceControllerDocs;
import insty.domain.user.dto.request.UserNotificationPreferenceUpdateReq;
import insty.domain.user.dto.response.UserNotificationPreferenceRes;
import insty.error.CommonErrorCode;
import insty.exception.CustomException;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @deprecated 더 이상 사용되지 않으며 추후 제거 예정.
 */
@Deprecated
@Slf4j
@RestController
@RequestMapping("/api/v1/users/notification-preferences")
@RequiredArgsConstructor
public class UserNotificationPreferenceController implements UserNotificationPreferenceControllerDocs {

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping
    public SuccessRes<UserNotificationPreferenceRes> getNotificationPreferences(@CurrentUser Long userId) {
        throw new CustomException(CommonErrorCode.DEPRECATED_API);
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PutMapping
    public SuccessRes<UserNotificationPreferenceRes> updateNotificationPreferences(
            @CurrentUser Long userId,
            @Valid @RequestBody UserNotificationPreferenceUpdateReq req
    ) {
        throw new CustomException(CommonErrorCode.DEPRECATED_API);
    }
}