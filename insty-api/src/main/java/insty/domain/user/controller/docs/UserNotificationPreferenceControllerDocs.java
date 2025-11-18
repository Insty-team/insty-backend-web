package insty.domain.user.controller.docs;

import insty.domain.user.dto.request.UserNotificationPreferenceUpdateReq;
import insty.domain.user.dto.response.UserNotificationPreferenceRes;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @deprecated 더 이상 사용되지 않으며 추후 제거 예정.
 */
@Deprecated
@Tag(name = "유저 알림 설정 API (사용중지)")
public interface UserNotificationPreferenceControllerDocs {

    @Operation(
        summary = "사용자 알림 설정 조회 (사용중지)",
        description = """
                현재 로그인한 사용자의 알림 설정을 조회합니다.
                
                알림 설정에는 다음 항목들이 포함됩니다:
                - 사용자 멘션 알림/이메일 수신 여부
                - 새 질문 알림/이메일 수신 여부  
                - 새 답변 알림/이메일 수신 여부
                - 답변 채택 알림/이메일 수신 여부
                
                설정이 없는 경우 기본값으로 모든 알림이 활성화된 설정이 자동 생성됩니다.
                """,
        security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_DETAIL)
    SuccessRes<UserNotificationPreferenceRes> getNotificationPreferences(@CurrentUser Long userId);

    @Operation(
        summary = "사용자 알림 설정 변경(사용중지)",
        description = """
                현재 로그인한 사용자의 알림 설정을 변경합니다.
                
                각 알림 유형별로 다음 두 가지 설정을 독립적으로 제어할 수 있습니다:
                1. 알림 활성화/비활성화 (앱 내 알림)
                2. 이메일 수신 활성화/비활성화
                
                이메일 알림은 다음 조건을 모두 만족해야 발송됩니다:
                - 해당 알림 유형의 이메일 수신이 활성화되어 있을 것
                - 사용자의 전체 이메일 수신 동의가 활성화되어 있을 것
                
                알림 유형:
                - 사용자 멘션: 댓글이나 답변에서 @멘션을 받았을 때
                - 새 질문: 관심있는 태그의 새로운 질문이 등록되었을 때  
                - 새 답변: 내가 작성한 질문에 새 답변이 달렸을 때
                - 답변 채택: 내가 작성한 답변이 채택되었을 때
                """,
        security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    SuccessRes<UserNotificationPreferenceRes> updateNotificationPreferences(
            @CurrentUser Long userId,
            UserNotificationPreferenceUpdateReq req
    );
}