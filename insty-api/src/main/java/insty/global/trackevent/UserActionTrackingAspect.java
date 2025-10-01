package insty.global.trackevent;

import insty.trackevent.TrackEvent;
import insty.trackevent.model.MixpanelEventType;
import insty.trackevent.port.AnalyticsEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserActionTrackingAspect {

    private static final String ATTR_URI_TEMPLATE_VARS = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
    private static final String PROP_MEMBER_ID = "memberId";
    private static final String PROP_HTTP_METHOD = "httpMethod";
    private static final String PROP_IP = "ip";
    private static final String PROP_PATH = "path";

    // external 모듈의 퍼블리셔 사용
    private final AnalyticsEventPublisher analyticsEventPublisher;

    // @TrackEvent가 붙은 메서드가 정상 리턴된 경우만 처리
    @AfterReturning(value = "@annotation(trackEvent)", argNames = "trackEvent")
    public void publishEventAfterSuccessfulReturn(final TrackEvent trackEvent) {
        // 인증 주체에서 memberId 추출
        Long authenticatedMemberId = extractAuthenticatedMemberIdFromSecurityContext();

        // Controller 를 통해 호출 시에 한정해서, 요청 객체 확보
        HttpServletRequest request = resolveCurrentHttpServletRequest();

        // 이벤트 속성 구성
        Map<String, Object> eventProperties = new HashMap<>();
        if (authenticatedMemberId != null) {
            eventProperties.put(PROP_MEMBER_ID, authenticatedMemberId);
        }
        if (request != null) {
            if (trackEvent.includeHttpMethod()) {
                eventProperties.put(PROP_HTTP_METHOD, request.getMethod());
            }
            if (trackEvent.includeRequestIp()) {
                eventProperties.put(PROP_IP, request.getRemoteAddr());
            }
            eventProperties.put(PROP_PATH, request.getRequestURI());
            eventProperties.putAll(extractPathVariablesByKeys(request, trackEvent.includePathVars()));
        }

        // 이벤트 타입 결정
        MixpanelEventType eventType = trackEvent.eventType();

        // 트랜잭션 커밋 이후 발행 보장(트랜잭션이 없으면 즉시 발행)
        Runnable publishTask = () -> analyticsEventPublisher.publish(eventType, authenticatedMemberId, eventProperties);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishTask.run();
                }
            });
        } else {
            publishTask.run();
        }
    }

    // SecurityContext 에서 memberId 추출
    private Long extractAuthenticatedMemberIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        try {
            Object principal = authentication.getPrincipal(); // AuthenticationMember 등 프로젝트 공용 Principal 가정
            var method = principal.getClass().getMethod("getMemberId");
            Object value = method.invoke(principal);
            return (value instanceof Number) ? ((Number) value).longValue() : null;
        } catch (Exception exception) {
            log.debug("Cannot extract memberId from principal: {}", exception.toString());
            return null;
        }
    }

    // 현재 요청 객체 조회
    private HttpServletRequest resolveCurrentHttpServletRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return (HttpServletRequest) attributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
    }

    // 지정된 키 배열에 해당하는 PathVariable만 추출
    private Map<String, Object> extractPathVariablesByKeys(HttpServletRequest request, String[] pathVariableKeys) {
        Map<String, Object> result = new HashMap<>();
        if (request == null || pathVariableKeys == null || pathVariableKeys.length == 0) {
            return result;
        }
        Object attribute = request.getAttribute(ATTR_URI_TEMPLATE_VARS);
        if (attribute instanceof Map<?, ?> map) {
            for (String key : pathVariableKeys) {
                Object value = map.get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }
}