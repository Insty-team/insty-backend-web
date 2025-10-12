package insty.global.trackevent;

import insty.trackevent.TrackEvent;
import insty.trackevent.model.MixpanelEventType;
import insty.trackevent.port.AnalyticsEventPublisher;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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

// @Valid 바인딩/파싱/타입/인증/인가/404 등 컨트롤러 진입 전 예외는 집계하지 않음 (Advice에서 별도 보강해야 함)
// 내부 비즈니스 로직에서의 성공/실패만 집계 (성공=커밋 후, 실패=롤백 후 또는 논TX 즉시)

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
        // 테스트 편의상 활성 플래그도 원복
        try {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        } catch (Throwable ignore) {}
    }

    @Test
        // 컨트롤러 경유 시 HTTP 메타데이터와 PathVariable 포함
    void controllerIncludesHttpMeta() throws Throwable {
        // given: 인증 주체/요청/PathVar 준비
        setPrincipal(777L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/courses/123");
        req.setRemoteAddr("203.0.113.10");
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("courseId", "123"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn(new Object()); // 정상 리턴

        TrackEvent ev = stub(
                MixpanelEventType.COURSE_LEARNING_STARTED,
                MixpanelEventType.NONE,
                new String[]{"courseId"},
                // TX가 없을 경우 즉시 발행
                true, true, true, false
        );

        // when: 어드바이스 호출
        aspect.publishMixpanelEvent(pjp, ev);

        // then: 이벤트 1회 발행 및 필드 검증
        verify(analyticsEventPublisher).publish(
                eventTypeCaptor.capture(),
                distinctIdCaptor.capture(),
                propertiesCaptor.capture()
        );
        Map<String, Object> props = propertiesCaptor.getValue();
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MixpanelEventType.COURSE_LEARNING_STARTED);
        assertThat(distinctIdCaptor.getValue()).isEqualTo(777L);
        assertThat(props.get("memberId")).isEqualTo(777L);
        assertThat(props.get("httpMethod")).isEqualTo("GET");
        assertThat(props.get("ip")).isEqualTo("203.0.113.10");
        assertThat(props.get("path")).isEqualTo("/api/courses/123");
        assertThat(String.valueOf(props.get("courseId"))).isEqualTo("123");
        // insert_id 는 매번 새 UUID
        assertThat(props.get("insert_id")).isInstanceOf(String.class);
        assertThat(((String) props.get("insert_id"))).hasSizeGreaterThan(10);
    }

    @Test
        // X-Forwarded-For 헤더가 있으면 첫 번째 IP 우선 사용
    void prefersXff() throws Throwable {
        // given: XFF 헤더 포함 요청
        setPrincipal(101L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/courses/1");
        req.setRemoteAddr("10.0.0.5");
        req.addHeader("X-Forwarded-For", "118.47.11.208, 203.0.113.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn(new Object());

        TrackEvent ev = stub(
                MixpanelEventType.COURSE_LEARNING_STARTED,
                MixpanelEventType.NONE,
                new String[]{},
                true, true, true, false
        );

        // when
        aspect.publishMixpanelEvent(pjp, ev);

        // then
        verify(analyticsEventPublisher).publish(any(), any(), propertiesCaptor.capture());
        assertThat(propertiesCaptor.getValue().get("ip")).isEqualTo("118.47.11.208");
    }

    @Test
        // Forwarded 헤더(for=...)에서 클라이언트 IP 파싱
    void parsesForwarded() throws Throwable {
        // given: Forwarded 헤더 포함 요청
        setPrincipal(102L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/courses/2");
        req.setRemoteAddr("10.0.0.6");
        req.addHeader("Forwarded", "for=\"203.0.113.195:1234\";proto=https, for=70.41.3.18");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn(new Object());

        TrackEvent ev = stub(
                MixpanelEventType.COURSE_LEARNING_STARTED,
                MixpanelEventType.NONE,
                new String[]{},
                true, true, true, false
        );

        // when
        aspect.publishMixpanelEvent(pjp, ev);

        // then
        verify(analyticsEventPublisher).publish(any(), any(), propertiesCaptor.capture());
        assertThat(propertiesCaptor.getValue().get("ip")).isEqualTo("203.0.113.195");
    }

    @Test
        // 서비스 계층에서 트랜잭션이 있을 경우, 커밋 후 발행
    void afterCommitInService() throws Throwable {
        // given: 트랜잭션 활성화
        setPrincipal(888L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent ev = stub(
                MixpanelEventType.COURSE_LEARNING_STARTED,
                MixpanelEventType.NONE,
                new String[]{},
                false, false, false, true // http/ip/path 제외, afterCommit만
        );
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("ok");

        // when: 호출 (커밋 전에는 발행 X)
        aspect.publishMixpanelEvent(pjp, ev);

        verify(analyticsEventPublisher, times(0)).publish(any(), any(), any());

        // when: 커밋 시뮬레이션
        List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
        // afterCompletion(STATUS_COMMITTED) 로 확정
        syncs.forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
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
        assertThat(props.get("insert_id")).isInstanceOf(String.class);
    }

    @Test
        // 트랜잭션 롤백: 실패 이벤트는 롤백 후 1회 발행
    void afterRollbackPublishesFailure() throws Throwable {
        // given: 트랜잭션 활성 + 실패 시나리오
        setPrincipal(111L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent ev = stub(
                MixpanelEventType.AUTH_SIGNED_UP,
                MixpanelEventType.AUTH_SIGNUP_FAILED,
                new String[]{},
                false, false, false, true // afterCommitOnly=true
        );
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenThrow(new IllegalStateException("boom"));

        // when: 호출 → 예외 재던짐 (테스트에서는 잡지 않고 흘려보냄)
        try {
            aspect.publishMixpanelEvent(pjp, ev);
        } catch (Exception ignored) {}

        // 롤백 확정
        List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
        syncs.forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);

        // then: 실패 이벤트 1회 발행, distinct_id는 null(퍼블리셔에서 anonymous 처리 가정)
        verify(analyticsEventPublisher).publish(
                eventTypeCaptor.capture(),
                distinctIdCaptor.capture(),
                propertiesCaptor.capture()
        );
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MixpanelEventType.AUTH_SIGNUP_FAILED);
        assertThat(distinctIdCaptor.getValue()).isNull();
        assertThat(propertiesCaptor.getValue().get("exception")).isEqualTo("IllegalStateException");
        assertThat(propertiesCaptor.getValue().get("insert_id")).isInstanceOf(String.class);
    }

    @Test
        // 트랜잭션 없음: 즉시 발행 (성공)
    void immediateSuccessNoTx() throws Throwable {
        // given: 트랜잭션/요청컨텍스트 없음
        setPrincipal(999L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent ev = stub(
                MixpanelEventType.AUTH_LOGGED_IN,
                MixpanelEventType.NONE,
                new String[]{},
                false, false, false, false // afterCommitOnly=false → 즉시 발행
        );

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("ok");

        // when
        aspect.publishMixpanelEvent(pjp, ev);

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
        assertThat(props.get("insert_id")).isInstanceOf(String.class);
    }

    @Test
        // 트랜잭션 없음: 즉시 발행 (실패)
    void immediateFailureNoTx() throws Throwable {
        // given
        setPrincipal(1001L);
        RequestContextHolder.resetRequestAttributes();
        TrackEvent ev = stub(
                MixpanelEventType.AUTH_SIGNED_UP,
                MixpanelEventType.AUTH_SIGNUP_FAILED,
                new String[]{},
                false, false, false, false // afterCommitOnly=false
        );

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenThrow(new RuntimeException("fail"));

        // when
        try {
            aspect.publishMixpanelEvent(pjp, ev);
        } catch (Exception ignored) {}

        // then: 즉시 실패 발행
        verify(analyticsEventPublisher).publish(
                eventTypeCaptor.capture(),
                distinctIdCaptor.capture(),
                propertiesCaptor.capture()
        );
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MixpanelEventType.AUTH_SIGNUP_FAILED);
        assertThat(distinctIdCaptor.getValue()).isNull();
        assertThat(propertiesCaptor.getValue().get("exception")).isEqualTo("RuntimeException");
        assertThat(propertiesCaptor.getValue().get("insert_id")).isInstanceOf(String.class);
    }

    // 테스트에 사용하는 유틸 ---

    // 간단한 Principal 스텁(Aspect가 getMemberId 리플렉션 호출)
    private interface HasMemberId { Long getMemberId(); }

    private void setPrincipal(Long memberId) {
        HasMemberId p = mock(HasMemberId.class);
        when(p.getMemberId()).thenReturn(memberId);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(p);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // @TrackEvent 익명 구현체 스텁 (새 시그니처 반영)
    private TrackEvent stub(MixpanelEventType success,
                            MixpanelEventType failure,
                            String[] pathVars,
                            boolean includeIp,
                            boolean includeMethod,
                            boolean includeEndpointPath,
                            boolean publishAfterCommitOnly) {
        return new TrackEvent() {
            @Override public MixpanelEventType successEventType() { return success; }
            @Override public MixpanelEventType failureEventType() { return failure; }
            @Override public String[] includePathVars() { return pathVars; }
            @Override public boolean includeRequestIp() { return includeIp; }
            @Override public boolean includeHttpMethod() { return includeMethod; }
            @Override public boolean includeEndpointPath() { return includeEndpointPath; }
            @Override public boolean includeTraceId() { return false; }
            @Override public boolean publishAfterCommitOnly() { return publishAfterCommitOnly; }
            @Override public TrackEvent.DistinctIdStrategy distinctIdStrategy() {
                return TrackEvent.DistinctIdStrategy.SECURITY_CONTEXT_MEMBER_ID;
            }
            @Override public Class<? extends Annotation> annotationType() { return TrackEvent.class; }
        };
    }
}