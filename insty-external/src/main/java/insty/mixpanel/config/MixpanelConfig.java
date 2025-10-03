package insty.mixpanel.config;

import insty.mixpanel.adapter.MixpanelEventPublisherAdapter;
import insty.trackevent.port.AnalyticsEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class MixpanelConfig {

    private static final String HOST_US = "api.mixpanel.com";
    private static final String HOST_EU = "api-eu.mixpanel.com";

    @Bean
    public AnalyticsEventPublisher analyticsEventPublisher(
            WebClient.Builder webClientBuilder,
            @Value("${mixpanel.enabled}") Boolean enabled,
            @Value("${mixpanel.token}") String projectToken,
            @Value("${mixpanel.residency}") String residency,
            @Value("${mixpanel.verbose}") Boolean verbose,
            @Value("${mixpanel.strict}") Boolean strict,
            @Value("${mixpanel.timeoutMillis}") Integer timeoutMillis
    ) {
        // 활성화 및 토큰 체크
        if (Boolean.FALSE.equals(enabled)) {
            log.warn("[Mixpanel] disabled by config. Tracking is OFF.");
            return (eventType, distinctId, props) -> { /* no-op */ };
        }
        if (!StringUtils.hasText(projectToken)) {
            log.warn("[Mixpanel] token is empty. Tracking will be skipped.");
            return (eventType, distinctId, props) -> { /* no-op */ };
        }

        // 데이터 레지던시 이후 호스트 분기
        final String host = "EU".equalsIgnoreCase(residency) ? HOST_EU : HOST_US;
        log.info("[Mixpanel] enabled. host={}, verbose={}, strict={}, timeoutMillis={}",
                host, verbose, strict, timeoutMillis);

        return new MixpanelEventPublisherAdapter(
                webClientBuilder.build(),
                projectToken,
                host,
                verbose,
                strict,
                enabled,
                timeoutMillis
        );
    }
}
