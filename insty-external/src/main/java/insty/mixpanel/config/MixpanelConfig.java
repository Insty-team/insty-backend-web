package insty.mixpanel.config;

import insty.mixpanel.adapter.MixpanelEventPublisherAdapter;
import insty.trackevent.port.AnalyticsEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MixpanelConfig {

    @Bean
    public AnalyticsEventPublisher analyticsEventPublisher(
            WebClient.Builder webClientBuilder,
            @Value("${app.mixpanel.token:}") String mixpanelToken
    ) {
        return new MixpanelEventPublisherAdapter(webClientBuilder.build(), mixpanelToken);
    }
}