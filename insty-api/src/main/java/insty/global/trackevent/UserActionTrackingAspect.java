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
    // Mixpanel 이벤트 지오IP 파싱용 표준 키
    private static final String PROP_IP = "ip";
    private static final String PROP_PATH = "path";

    private static final String HDR_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HDR_X_REAL_IP = "X-Real-IP";
    private static final String HDR_CF_CONNECTING_IP = "CF-Connecting-IP";
    private static final String HDR_TRUE_CLIENT_IP = "True-Client-IP";
    private static final String HDR_FORWARDED = "Forwarded";   // RFC 7239: for=1.2.3.4;proto=https

    // external 모듈의 퍼블리셔 사용
    private final AnalyticsEventPublisher analyticsEventPublisher;

    // @TrackEvent가 붙은 메서드가 정상 리턴된 경우만 처리
    @AfterReturning(value = "@annotation(trackEvent)", argNames = "trackEvent")
    public void publishMixpanelEvent(final TrackEvent trackEvent) {
        // 인증 주체에서 memberId 추출
        Long authenticatedMemberId = extractAuthenticatedMemberId();

        // 현재 요청 객체 확보(Controller 경유 시)
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
                String clientIp = resolveClientIp(request);
                if (clientIp != null && !clientIp.isBlank()) {
                    eventProperties.put(PROP_IP, clientIp); // 여기서 $ip로 전달
                }
            }
            eventProperties.put(PROP_PATH, request.getRequestURI());
            eventProperties.putAll(extractPathVariables(request, trackEvent.includePathVars()));
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