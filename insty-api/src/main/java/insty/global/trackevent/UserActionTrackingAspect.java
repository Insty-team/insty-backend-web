package insty.global.trackevent;

import insty.trackevent.TrackEvent;
import insty.trackevent.model.MixpanelEventType;
import insty.trackevent.port.AnalyticsEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
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
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserActionTrackingAspect {

    private static final String ATTR_URI_TEMPLATE_VARS = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

    private static final String PROP_MEMBER_ID = "memberId";
    private static final String PROP_HTTP_METHOD = "httpMethod";
    // Mixpanel 이벤트 지오IP 파싱용 표준 키
    private static final String PROP_IP = "$ip";
    private static final String PROP_PATH = "path";
    // 실패 이벤트에 사용(민감정보 금지, 클래스명 수준)
    private static final String PROP_EXCEPTION = "exception";
    private static final String PROP_INSERT_ID = "$insert_id";

    private static final String HDR_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HDR_X_REAL_IP = "X-Real-IP";
    private static final String HDR_CF_CONNECTING_IP = "CF-Connecting-IP";
    private static final String HDR_TRUE_CLIENT_IP = "True-Client-IP";
    private static final String HDR_FORWARDED = "Forwarded";   // RFC 7239: for=1.2.3.4;proto=https
    private static final String PROP_TRACE_ID = "trace_id";
    private static final String HDR_X_REQUEST_ID = "X-Request-Id";

    // external 모듈의 퍼블리셔 사용
    private final AnalyticsEventPublisher analyticsEventPublisher;

    // 실패 예외를 롤백 후 사용하기 위해 보관
    private static final ThreadLocal<Exception> LAST_FAILURE = new ThreadLocal<>();

    // @Around로 변경하여 성공/실패 모두 처리(트랜잭션 결과 기준 afterCompletion 사용)
    @Around(value = "@annotation(trackEvent)")
    public Object publishMixpanelEvent(final ProceedingJoinPoint pjp, final TrackEvent trackEvent) throws Throwable {
        // 인증 주체에서 memberId 추출
        Long authenticatedMemberId = extractAuthenticatedMemberId();

        // 현재 요청 객체 확보(Controller 경유 시)
        HttpServletRequest request = resolveCurrentHttpServletRequest();

        // 이벤트 속성 구성(공통)
        Map<String, Object> baseProperties = new HashMap<>();
        if (authenticatedMemberId != null) {
            baseProperties.put(PROP_MEMBER_ID, authenticatedMemberId);
        }
        if (request != null) {
            if (trackEvent.includeHttpMethod()) {
                baseProperties.put(PROP_HTTP_METHOD, request.getMethod());
            }
            if (trackEvent.includeRequestIp()) {
                String clientIp = resolveClientIp(request);
                if (clientIp != null && !clientIp.isBlank()) {
                    baseProperties.put(PROP_IP, clientIp); // 여기서 $ip로 전달
                }
            }
            if (trackEvent.includeEndpointPath()) { // 추가 옵션 처리
                baseProperties.put(PROP_PATH, request.getRequestURI());
            }
            baseProperties.putAll(extractPathVariables(request, trackEvent.includePathVars()));
        }

        // trace_id 채우기 (X-Request-Id 우선, 없으면 MDC("traceId"))
        // 어노테이션 옵션이 true일 때만 trace_id를 붙임
        if (trackEvent.includeTraceId()) {
            String traceId = null;
            // HTTP 요청이 있는 경우, 게이트웨이/프록시/로드밸런서가 심어 준 X-Request-Id 헤더를 먼저 사용
            if (request != null) {
                traceId = request.getHeader(HDR_X_REQUEST_ID);
            }
            // 헤더가 없거나 비어 있으면 애플리케이션 레벨의 추적 값으로 폴백
            if (!isNotBlank(traceId)) {
                try {
                    traceId = MDC.get("traceId");
                } catch (Throwable ignored) {}
            }
            if (isNotBlank(traceId)) {
                baseProperties.put(PROP_TRACE_ID, traceId);
            }
        }

        // 트랜잭션 활성 여부
        final boolean txActiveAndSync =
                trackEvent.publishAfterCommitOnly() &&
                        TransactionSynchronizationManager.isActualTransactionActive() &&
                        TransactionSynchronizationManager.isSynchronizationActive();

        // 트랜잭션이 활성화된 경우, '결과 확정 후' 발행을 위해 afterCompletion 등록
        if (txActiveAndSync) {
            final MixpanelEventType successType = trackEvent.successEventType();
            final MixpanelEventType failureType = trackEvent.failureEventType();
            final Long distinctIdOnSuccess = resolveDistinctIdForSuccess(trackEvent, authenticatedMemberId);
            final Map<String, Object> snapshotProps = new HashMap<>(baseProperties);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    try {
                        if (status == TransactionSynchronization.STATUS_COMMITTED && successType != MixpanelEventType.NONE) {
                            Map<String, Object> props = new HashMap<>(snapshotProps);
                            props.put(PROP_INSERT_ID, UUID.randomUUID().toString());
                            analyticsEventPublisher.publish(successType, distinctIdOnSuccess, props);
                        } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK && failureType != MixpanelEventType.NONE) {
                            Map<String, Object> props = new HashMap<>(snapshotProps);
                            props.put(PROP_INSERT_ID, UUID.randomUUID().toString());
                            Exception ex = LAST_FAILURE.get();
                            if (ex != null) {
                                props.put(PROP_EXCEPTION, ex.getClass().getSimpleName()); // 민감정보 X
                            }
                            // 실패 시 퍼블리셔 구현에서 anonymous 처리 권장(null 전달)
                            analyticsEventPublisher.publish(failureType, null, props);
                        }
                    } catch (Exception publishEx) {
                        log.warn("[TrackEvent] publish afterCompletion error={}", publishEx.toString());
                    } finally {
                        LAST_FAILURE.remove();
                    }
                }
            });
        }

        try {
            // 실제 비즈니스 실행
            Object result = pjp.proceed();

            // 트랜잭션 없거나 동기화 비활성 시, 성공 이벤트 즉시 발행
            if (!txActiveAndSync) {
                MixpanelEventType successType = trackEvent.successEventType();
                if (successType != MixpanelEventType.NONE) {
                    Map<String, Object> props = new HashMap<>(baseProperties);
                    props.put(PROP_INSERT_ID, UUID.randomUUID().toString());
                    Long distinctId = resolveDistinctIdForSuccess(trackEvent, authenticatedMemberId);
                    try {
                        analyticsEventPublisher.publish(successType, distinctId, props);
                    } catch (Exception publishEx) {
                        log.warn("[TrackEvent] publish immediate success error type={} error={}",
                                successType, publishEx.toString());
                    }
                }
            }

            return result;
        } catch (IllegalArgumentException devError) {
            // 개발 시 나온 오류의 경우, 이벤트를 발행하지 않고 IllegalArgumentException 반환
            throw devError;

        } catch (Exception ex) {
            // 실패 예외 저장(롤백 후 afterCompletion에서 사용)
            LAST_FAILURE.set(ex);

            // 트랜잭션이 없으면(혹은 동기화 비활성) 즉시 실패 이벤트 발행으로 폴백
            if (!txActiveAndSync && trackEvent.failureEventType() != MixpanelEventType.NONE) {
                Map<String, Object> props = new HashMap<>(baseProperties);
                props.put(PROP_INSERT_ID, UUID.randomUUID().toString());
                props.put(PROP_EXCEPTION, ex.getClass().getSimpleName());
                try {
                    analyticsEventPublisher.publish(trackEvent.failureEventType(), null, props);
                } catch (Exception publishEx) {
                    log.warn("[TrackEvent] publish immediate failure error type={} error={}",
                            trackEvent.failureEventType(), publishEx.toString());
                } finally {
                    LAST_FAILURE.remove();
                }
            }
            throw ex;
        } finally {
            // 트랜잭션 경계가 아니었던 경우 등을 대비한 정리
            if (!txActiveAndSync) {
                LAST_FAILURE.remove();
            }
        }
    }

    // 성공 이벤트용 distinct_id 계산: 전략이 ANONYMOUS면 null, 아니면 인증된 memberId
    private Long resolveDistinctIdForSuccess(TrackEvent trackEvent, Long authenticatedMemberId) {
        return (trackEvent.distinctIdStrategy() == TrackEvent.DistinctIdStrategy.ANONYMOUS)
                ? null
                : authenticatedMemberId;
    }

    // SecurityContext 에서 memberId 추출
    private Long extractAuthenticatedMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        try {
            Object principal = authentication.getPrincipal();
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
    private Map<String, Object> extractPathVariables(HttpServletRequest request, String[] pathVariableKeys) {
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

    // 프록시 헤더 우선, 폴백 순으로 IP 결정
    private String resolveClientIp(HttpServletRequest request) {
        // X-Forwarded-For: "client, proxy1, proxy2" 형태 → 첫 번째가 클라이언트
        String xff = request.getHeader(HDR_X_FORWARDED_FOR);
        if (isNotBlank(xff)) {
            String candidate = xff.split(",")[0].trim();
            String ip = sanitizeIpCandidate(candidate);
            if (isNotBlank(ip)) return ip;
        }

        // X-Real-IP
        String realIp = request.getHeader(HDR_X_REAL_IP);
        if (isNotBlank(realIp)) {
            String ip = sanitizeIpCandidate(realIp);
            if (isNotBlank(ip)) return ip;
        }

        // CDN/프록시 헤더들
        String cfIp = request.getHeader(HDR_CF_CONNECTING_IP);
        if (isNotBlank(cfIp)) {
            String ip = sanitizeIpCandidate(cfIp);
            if (isNotBlank(ip)) return ip;
        }
        String trueClientIp = request.getHeader(HDR_TRUE_CLIENT_IP);
        if (isNotBlank(trueClientIp)) {
            String ip = sanitizeIpCandidate(trueClientIp);
            if (isNotBlank(ip)) return ip;
        }

        // RFC 7239 Forwarded: for=1.2.3.4;proto=https
        String forwarded = request.getHeader(HDR_FORWARDED);
        if (isNotBlank(forwarded)) {
            String ip = extractClientIpFromForwardedHeader(forwarded);
            if (isNotBlank(ip)) return ip;
        }

        // 폴백: RemoteAddr
        return sanitizeIpCandidate(request.getRemoteAddr());
    }

    // Forwarded 헤더에서 for= 값 파싱
    private String extractClientIpFromForwardedHeader(String forwardedHeaderValue) {
        // 다중 값일 수 있으므로 세미콜론/콤마 기준 토큰화
        // 예: "for=\"203.0.113.195:1234\";proto=https, for=70.41.3.18"
        String lower = forwardedHeaderValue.toLowerCase();
        int idx = lower.indexOf("for=");
        if (idx < 0) return null;

        String after = forwardedHeaderValue.substring(idx + 4).trim(); // 4 == len("for=")
        // 토큰 끝(;, ,)까지 잘라내기
        int semi = after.indexOf(';');
        int comma = after.indexOf(',');
        int end = (semi < 0) ? comma : (comma < 0 ? semi : Math.min(semi, comma));
        String token = (end < 0) ? after : after.substring(0, end);
        return sanitizeIpCandidate(token);
    }

    // IP 후보 문자열 정리: 양끝 따옴표/대괄호 제거, 포트 제거(IPv4:port)
    private String sanitizeIpCandidate(String candidate) {
        if (!isNotBlank(candidate)) return null;
        String ip = candidate.trim();

        // 양끝 따옴표 제거
        if (ip.startsWith("\"") && ip.endsWith("\"") && ip.length() >= 2) {
            ip = ip.substring(1, ip.length() - 1);
        }

        // IPv6 대괄호 제거: [2001:db8::1]
        if (ip.startsWith("[") && ip.contains("]")) {
            ip = ip.substring(1, ip.indexOf(']'));
        }

        // IPv4:port 형태에서 포트 제거
        int lastColon = ip.lastIndexOf(':');
        if (lastColon > -1 && ip.indexOf(':') == lastColon && ip.contains(".")) {
            // 콜론이 하나만 있고 점이 있다 → IPv4:port로 가정
            ip = ip.substring(0, lastColon);
        }

        // 공백/빈문자 처리
        return isNotBlank(ip) ? ip : null;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}