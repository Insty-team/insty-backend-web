package insty.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class AiApiClientConfig {

    @Value("${app.domain}")
    private String domain;

    @Value("${app.ai-api-secret}")
    private String aiSecretKey;

    @Bean
    public WebClient aiApiWebClient() {
        return WebClient.builder()
                .baseUrl("https://" + domain)
                .defaultHeader("Authorization", "Bearer " + aiSecretKey)
                .build();
    }
}
