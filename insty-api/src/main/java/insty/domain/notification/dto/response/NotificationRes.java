package insty.domain.notification.dto.response;

import insty.model.notification.Notification;
import insty.model.notification.NotificationState;
import insty.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "알림 조회 요청 결과")
public record NotificationRes(
        Long id,

        @Schema(description = """
                알림 타입:
                - NEW_COURSE: 새 강의 알림 - 새로운 강의가 등록되었을 때 수강중인 Runner에게 발송 (수신자 : RUNNER)
                - NEW_COURSE_QUESTION: 새 질문 알림 - 구독/관심있는 커뮤니티에 새로운 질문이 등록되었을 때 발송 (수신자 : CREATOR)
                - NEW_COURSE_ANSWER: 새 답변 알림 - 내가 작성한 질문에 새로운 답변이 달렸을 때 발송 (수신자 : RUNNER / CREATOR)
                - COURSE_ANSWER_ACCEPT: 답변 채택 알림 - 내가 작성한 답변이 채택되었을 때 발송 (수신자 : RUNNER)
                - USER_MENTIONED: 멘션 알림 - 커뮤니티 질문/답변에서 다른 사용자가 나를 멘션했을 때 발송 (수신자 : RUNNER / CREATOR)
                """,
                example = "NEW_COURSE_ANSWER")
        NotificationType type,

        @Schema(description = "알림 제목")
        String title,

        @Schema(description = "알림 상세 메시지")
        String message,

        @Schema(description = "알림 선택시 이동할 redirect-url")
        String redirectUrl,

        @Schema(description = "알림 조회 상태")
        boolean isRead,

        @Schema(description = "알림 생성일")
        Instant createdAt
) {
    public static NotificationRes from(
            Notification notification
    ){
        return new NotificationRes(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRedirectUrl(),
                notification.getState().equals(NotificationState.READ),
                notification.getCreatedAt()
        );
    }

}
