package insty.global.config;

import insty.global.converter.OctetStreamReadMsgConverter;
import insty.global.security.resolver.CurrentUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.security.cors.allowed-origins}")
    private String[]  ALLOW_CROSS_ORIGIN_DOMAIN;

    @Value("${spring.security.cors.allowed-methods}")
    private String[] ALLOW_METHODS;

    // 시큐리티
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    // 스웨거
    private final OctetStreamReadMsgConverter octetStreamReadMsgConverter;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOW_CROSS_ORIGIN_DOMAIN)
                .allowedMethods(ALLOW_METHODS)
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(octetStreamReadMsgConverter);
    }
}