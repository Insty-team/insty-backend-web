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
import org.mockito.Captor;
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

    @InjectMocks private UserActionTrackingAspect aspect;
    @Mock private AnalyticsEventPublisher analyticsEventPublisher;
    @Captor private ArgumentCaptor<MixpanelEventType> eventTypeCaptor;
    @Captor private ArgumentCaptor<Long> distinctIdCaptor;
    @Captor private ArgumentCaptor<Map<String, Object>> propertiesCaptor;

    @AfterEach
    void clean() {
        // 테스트 간 컨텍스트/트랜잭션 상태 초기화
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
        // 컨트롤러 경유 시 HTTP 메타데이터와 PathVariable 포함
    void controllerIncludesHttpMeta() {
        // given: 인증 주체/요청/PathVar 준비
        setPrincipal(777L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/courses/123");
        req.setRemoteAddr("203.0.113.10");
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("courseId", "123"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        TrackEvent ev = stub(MixpanelEventType.COURSE_VIEWED, new String[]{"courseId"}, true, true);

        // when: 어드바이스 호출
        aspect.publishMixpanelEvent(ev);

        // then: 이벤트 1회 발행 및 필드 검증
        verify(analyticsEventPublisher).publish(
                eventTypeCaptor.capture(),
                distinctIdCaptor.capture(),
                propertiesCaptor.capture()
        );
        Map<String, Object> props = propertiesCaptor.getValue();
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MixpanelEventType.COURSE_VIEWED);
        assertThat(distinctIdCaptor.getValue()).isEqualTo(777L);
        assertThat(props.get("memberId")).isEqualTo(777L);
        assertThat(props.get("httpMethod")).isEqualTo("GET");
        assertThat(props.get("ip")).isEqualTo("203.0.113.10");
        assertThat(props.get("path")).isEqualTo("/api/courses/123");
        assertThat(String.valueOf(props.get("courseId"))).isEqualTo("123");
    }

    @Test
        // X-Forwarded-For 헤더가 있으면 첫 번째 IP 우선 사용
    void prefersXff() {
        // given: XFF 헤더 포함 요청
        setPrincipal(101L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/courses/1");
        req.setRemoteAddr("10.0.0.5");
        req.addHeader("X-Forwarded-For", "118.47.11.208, 203.0.113.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        TrackEvent ev = stub(MixpanelEventType.COURSE_VIEWED, new String[]{}, true, true);

        // when
        aspect.publishMixpanelEvent(ev);

        // then
        verify(analyticsEventPublisher).publish(any(), any(), propertiesCaptor.capture());
        assertThat(propertiesCaptor.getValue().get("ip")).isEqualTo("118.47.11.208");
    }

    @Test
        // Forwarded 헤더(for=...)에서 클라이언트 IP 파싱
    void parsesForwarded() {
        // given: Forwarded 헤더 포함 요청
        setPrincipal(102L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/courses/2");
        req.setRemoteAddr("10.0.0.6");
        req.addHeader("Forwarded", "for=\"203.0.113.195:1234\";proto=https, for=70.41.3.18");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        TrackEvent ev = stub(MixpanelEventType.COURSE_VIEWED, new String[]{}, true, true);

        // when
        aspect.publishMixpanelEvent(ev);

        // then
        verify(analyticsEventPublisher).publish(any(), any(), propertiesCaptor.capture());
        assertThat(propertiesCaptor.getValue().get("ip")).isEqualTo("203.0.113.195");
    }

    @Test
        // 서비스 계층에서 트랜잭션이 있을 경우, 커밋 후 발행
    void afterCommitInService() {
        // given: 트랜잭션 활성화
        setPrincipal(888L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent ev = stub(MixpanelEventType.COURSE_LEARNING_STARTED, new String[]{}, false, false);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        // when: 커밋 전 호출
        aspect.publishMixpanelEvent(ev);

        // then: 커밋 전에는 발행 안 됨
        verify(analyticsEventPublisher, times(0)).publish(any(), any(), any());

        // when: 커밋 시뮬레이션
        List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
        syncs.forEach(TransactionSynchronization::afterCommit);
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);

        // then: 커밋 후 1회 발행 + HTTP 메타 없음
        verify(analyticsEventPublisher).publish(
                eventTypeCaptor.capture(),
                distinctIdCaptor.capture(),
                propertiesCaptor.capture()
        );
        Map<String, Object> props = propertiesCaptor.getValue();
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MixpanelEventType.COURSE_LEARNING_STARTED);
        assertThat(distinctIdCaptor.getValue()).isEqualTo(888L);
        assertThat(props.get("memberId")).isEqualTo(888L);
        assertThat(props.containsKey("httpMethod")).isFalse();
        assertThat(props.containsKey("path")).isFalse();
        assertThat(props.containsKey("ip")).isFalse();
    }

    @Test
        // 트랜잭션 없음: 즉시 발행
    void immediateNoTx() {
        // given: 트랜잭션/요청컨텍스트 없음
        setPrincipal(999L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent ev = stub(MixpanelEventType.AUTH_LOGGED_IN, new String[]{}, false, false);

        // when
        aspect.publishMixpanelEvent(ev);

        // then
        verify(analyticsEventPublisher).publish(
                eventTypeCaptor.capture(),
                distinctIdCaptor.capture(),
                propertiesCaptor.capture()
        );
        Map<String, Object> props = propertiesCaptor.getValue();
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MixpanelEventType.AUTH_LOGGED_IN);
        assertThat(distinctIdCaptor.getValue()).isEqualTo(999L);
        assertThat(props.get("memberId")).isEqualTo(999L);
    }

    // 간단한 Principal 스텁(Aspect가 getMemberId 리플렉션 호출)
    private interface HasMemberId { Long getMemberId(); }

    private void setPrincipal(Long memberId) {
        HasMemberId p = mock(HasMemberId.class);
        when(p.getMemberId()).thenReturn(memberId);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(p);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // @TrackEvent 익명 구현체 스텁
    private TrackEvent stub(MixpanelEventType type, String[] pathVars, boolean includeIp, boolean includeMethod) {
        return new TrackEvent() {
            @Override public MixpanelEventType eventType() { return type; }
            @Override public String[] includePathVars() { return pathVars; }
            @Override public boolean includeRequestIp() { return includeIp; }
            @Override public boolean includeHttpMethod() { return includeMethod; }
            @Override public Class<? extends Annotation> annotationType() { return TrackEvent.class; }
        };
    }
}
