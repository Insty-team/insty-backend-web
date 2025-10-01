package insty.trackevent;

import insty.trackevent.model.MixpanelEventType;
import java.lang.annotation.*;

// 특정 메서드가 정상 종료되었을 때 이벤트 트래킹을 트리거하는 어노테이션
@Target(ElementType.METHOD)               // 메서드에만 사용
@Retention(RetentionPolicy.RUNTIME)       // 런타임까지 유지(AOP 인식)
@Documented                               // Javadoc 문서에 포함
public @interface TrackEvent {

    // 발행할 이벤트 종류(필수)
    MixpanelEventType eventType();

    // 이벤트 속성에 포함할 PathVariable 키 배열
    String[] includePathVars() default {};

    // 요청 IP를 이벤트 속성에 포함할지 여부
    boolean includeRequestIp() default true;

    // HTTP 메서드(GET/POST 등) 포함 여부
    boolean includeHttpMethod() default true;
}