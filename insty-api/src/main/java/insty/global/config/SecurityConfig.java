package insty.global.config;

import insty.global.security.LoginAuthenticationProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.cors.allowed-origins}")
    private List<String> ALLOW_CROSS_ORIGIN_DOMAIN;

    @Value("${spring.security.cors.allowed-methods}")
    private List<String> ALLOW_METHODS;

    @Value("${app.health-check-path}")
    private String HEALTH_CHECK_PATH;

    private final UserDetailsService userDetailsService;


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 관리자
    @Bean
    public AuthenticationManager authenticationManager() {
        LoginAuthenticationProvider authenticationProvider = new LoginAuthenticationProvider(userDetailsService, bCryptPasswordEncoder());

        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }


    // 시큐리티 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 허용 URL
        final String[] WHITE_LIST_URL = new String[]{"/api/v1/**", HEALTH_CHECK_PATH};
        // 스웨거 허용 URL
        final String[] SWAGGER_LIST_URL = new String[]{"/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
                "/webjars/**"};

        // CORS 설정
        http.cors((cors -> cors.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(ALLOW_CROSS_ORIGIN_DOMAIN);            // Cors 도메인 (Credentials 때문에 필수)
                configuration.setAllowedMethods(ALLOW_METHODS);            // HTTP 메서드
                configuration.setAllowedHeaders(List.of(CorsConfiguration.ALL));                 // 모든 헤더 허용
                configuration.setAllowCredentials(true);                    // 인증 관련 정보 (JWT, 세션 쿠키 받기 위함)
                configuration.setMaxAge(3600L);                         // 브라우저의 preflight 요청 캐싱 시간

                return configuration;
            }
        })));

        // Form 형식이 아니기 때문에 disabled 처리
        http
            .csrf((csrf) -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());


        // 인증 커스텀 (hasRole : 역할, hasAuthority : 권한)
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/v1/users/login", "/api/v1/users/logout").permitAll()       // 로그인, 로그아웃
                .requestMatchers(WHITE_LIST_URL).permitAll()       // 누구나 접근 가능
                .requestMatchers(SWAGGER_LIST_URL).permitAll()       // 누구나 접근 가능
                .anyRequest().authenticated()                // 이외의 경로 모두 인증 필요
        );


        return http.build();
    }
}
