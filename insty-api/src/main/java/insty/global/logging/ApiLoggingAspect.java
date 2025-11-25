package insty.global.logging;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@ConditionalOnProperty(name = "aop.api-logging.enabled", havingValue = "true", matchIfMissing = true)
public class ApiLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("insty.aop.API");

    @PostConstruct
    public void init() {
        log.info("ApiLoggingAspect 초기화 됨");
    }

    private static final AtomicLong REQUEST_ID_COUNTER = new AtomicLong(0);

    @Pointcut("execution(* insty.domain.*.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        long requestId = REQUEST_ID_COUNTER.incrementAndGet();
        String id = String.format("%03d", requestId);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("ID={} |  Endpoint={} {} | | User IP={} | Time={}ms",
                    id,
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("ID={} | Time={}ms | Error={}", id, duration, e.getMessage());
            throw e;
        }
    }
}
