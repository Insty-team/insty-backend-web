package insty.trackevent;

import insty.trackevent.model.MixpanelEventType;
import java.lang.annotation.*;

// 특정 메서드가 정상 종료되었을 때 이벤트 트래킹을 트리거하는 어노테이션
@Target(ElementType.METHOD)               // 메서드에만 사용
@Retention(RetentionPolicy.RUNTIME)       // 런타임까지 유지(AOP 인식)
@Documented                               // Javadoc 문서에 포함
public @interface TrackEvent {

    // 성공 시 발행할 이벤트 (필수)
    MixpanelEventType successEventType();

    // 실패 시 발행할 이벤트 (선택) - 지정 시 예외 발생하면 실패 이벤트 발행
    MixpanelEventType failureEventType() default MixpanelEventType.NONE;

    // PathVariable 키 배열(성공/실패 공통으로 properties에 포함)
    String[] includePathVars() default {};

    // IP, HTTP, 엔드포인트 등 부가정보 저장
    boolean includeRequestIp() default true;
    boolean includeHttpMethod() default true;
    boolean includeEndpointPath() default true;
    boolean includeTraceId() default true;

    // 트랜잭션 커밋 이후에만 발행할지 여부
    boolean publishAfterCommitOnly() default true;

    // distinct_id 계산 전략: SECURITY_CONTEXT_MEMBER_ID | ANONYMOUS
    DistinctIdStrategy distinctIdStrategy() default DistinctIdStrategy.SECURITY_CONTEXT_MEMBER_ID;

    enum DistinctIdStrategy {
        SECURITY_CONTEXT_MEMBER_ID, // AuthenticationMember에서 memberId 추출
        ANONYMOUS                   // 실패 등 memberId 없는 경우
    }
}