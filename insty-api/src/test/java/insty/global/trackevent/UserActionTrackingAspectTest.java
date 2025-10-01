package insty.global.trackevent;

import insty.trackevent.TrackEvent;
import insty.trackevent.model.MixpanelEventType;
import insty.trackevent.port.AnalyticsEventPublisher;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserActionTrackingAspectTest {

    @InjectMocks
    private UserActionTrackingAspect aspect;

    @Mock
    private AnalyticsEventPublisher analyticsEventPublisher;

    @AfterEach
    // 테스트 간 영향을 주지 않게 격리하는 용도
    void cleanContexts() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    // Controller 에서 @TrackEvent(COURSE_VIEWED) 정상 종료 시, 이벤트 1회 발행 및 memberId/httpMethod/ip/path/courseId 포함 검증
    void trackEvent_controller() {
        // given: 인증 주체(777) 세팅 + HTTP 요청 컨텍스트/PathVariable + TrackEvent 준비
        setSecurityPrincipalWithMemberId(777L);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/courses/123");
        request.setRemoteAddr("203.0.113.10");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("courseId", "123"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        TrackEvent trackEvent = stubTrackEvent(
                MixpanelEventType.COURSE_VIEWED,
                new String[] {"courseId"},
                true,
                true
        );

        // when: 어드바이스 직접 호출
        aspect.publishEventAfterSuccessfulReturn(trackEvent);

        // then: 퍼블리셔 1회 호출 및 메타데이터/PathVariable 포함값 검증
        ArgumentCaptor<MixpanelEventType> typeCaptor = ArgumentCaptor.forClass(MixpanelEventType.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(analyticsEventPublisher, times(1))
                .publish(typeCaptor.capture(), idCaptor.capture(), propsCaptor.capture());

        assertThat(typeCaptor.getValue()).isEqualTo(MixpanelEventType.COURSE_VIEWED);
        assertThat(idCaptor.getValue()).isEqualTo(777L);

        Map<String, Object> props = propsCaptor.getValue();
        assertThat(props.get("memberId")).isEqualTo(777L);
        assertThat(props.get("httpMethod")).isEqualTo("GET");
        assertThat(props.get("ip")).isEqualTo("203.0.113.10");
        assertThat(props.get("path")).isEqualTo("/api/courses/123");
        assertThat(String.valueOf(props.get("courseId"))).isEqualTo("123");
    }

    // Service 에서 @TrackEvent(COURSE_LEARNING_STARTED) 정상 종료 시, 커밋 이후에만 1회 발행되고 HTTP 메타데이터는 포함되지 않는지 검증
    @Test
    void trackEvent_service() {
        // given: 인증 주체(888) + 요청 컨텍스트 제거 + TrackEvent 준비 + 트랜잭션 동기화/활성 플래그 설정
        setSecurityPrincipalWithMemberId(888L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent trackEvent = stubTrackEvent(MixpanelEventType.COURSE_LEARNING_STARTED, new String[] {}, false, false);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        // when: 어드바이스 직접 호출
        aspect.publishEventAfterSuccessfulReturn(trackEvent);

        // then: 커밋 전에는 퍼블리셔 호출 없음
        verify(analyticsEventPublisher, times(0)).publish(any(), any(), any());

        // when: afterCommit 시그널 수동 호출(커밋 시뮬레이션)
        List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
        syncs.forEach(TransactionSynchronization::afterCommit);
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);

        // then: 커밋 후 1회 호출 및 memberId만 포함(HTTP 메타데이터 없음)
        ArgumentCaptor<MixpanelEventType> typeCaptor = ArgumentCaptor.forClass(MixpanelEventType.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(analyticsEventPublisher, times(1))
                .publish(typeCaptor.capture(), idCaptor.capture(), propsCaptor.capture());

        assertThat(typeCaptor.getValue()).isEqualTo(MixpanelEventType.COURSE_LEARNING_STARTED);
        assertThat(idCaptor.getValue()).isEqualTo(888L);

        Map<String, Object> props = propsCaptor.getValue();
        assertThat(props.get("memberId")).isEqualTo(888L);
        assertThat(props.containsKey("httpMethod")).isFalse();
        assertThat(props.containsKey("path")).isFalse();
        assertThat(props.containsKey("ip")).isFalse();
    }

    // 트랜잭션이 없을 때 @TrackEvent(AUTH_LOGGED_IN) 정상 종료 시, 이벤트가 즉시 1회 발행되고 memberId가 포함되는지 검증
    @Test
    void trackEvent_noTransaction() {
        // given: 인증 주체(999) 세팅 + 요청 컨텍스트 제거 + TrackEvent 준비
        setSecurityPrincipalWithMemberId(999L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent trackEvent = stubTrackEvent(MixpanelEventType.AUTH_LOGGED_IN, new String[] {}, false, false);

        // when: 어드바이스 직접 호출
        aspect.publishEventAfterSuccessfulReturn(trackEvent);

        // then: 퍼블리셔 1회 호출 및 memberId 포함 검증
        ArgumentCaptor<MixpanelEventType> typeCaptor = ArgumentCaptor.forClass(MixpanelEventType.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(analyticsEventPublisher, times(1))
                .publish(typeCaptor.capture(), idCaptor.capture(), propsCaptor.capture());

        assertThat(typeCaptor.getValue()).isEqualTo(MixpanelEventType.AUTH_LOGGED_IN);
        assertThat(idCaptor.getValue()).isEqualTo(999L);
        assertThat(propsCaptor.getValue().get("memberId")).isEqualTo(999L);
    }

    // 테스트용 Principal 인터페이스(Aspect가 리플렉션으로 호출하는 getMemberId 제공)
    private interface HasMemberId { Long getMemberId(); }

    // SecurityContext에 memberId를 가진 Principal 목 객체를 주입
    private void setSecurityPrincipalWithMemberId(Long memberId) {
        HasMemberId principal = mock(HasMemberId.class);
        when(principal.getMemberId()).thenReturn(memberId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // @TrackEvent 어노테이션 값을 대신할 익명 구현체를 생성하여 스텁으로 제공
    private TrackEvent stubTrackEvent(
            MixpanelEventType eventType,
            String[] includePathVars,
            boolean includeIp,
            boolean includeHttpMethod
    ) {
        return new TrackEvent() {
            @Override public MixpanelEventType eventType() { return eventType; }
            @Override public String[] includePathVars() { return includePathVars; }
            @Override public boolean includeRequestIp() { return includeIp; }
            @Override public boolean includeHttpMethod() { return includeHttpMethod; }
            @Override public Class<? extends Annotation> annotationType() { return TrackEvent.class; }
        };
    }
}
