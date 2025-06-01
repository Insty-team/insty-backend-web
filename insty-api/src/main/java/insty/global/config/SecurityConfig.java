package insty.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.global.security.LoginAuthenticationFilter;
import insty.global.security.LoginFailHandler;
import insty.global.security.LoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    // 시큐리티에게 AuthenticationConfiguration 주입 받기
    private final AuthenticationConfiguration authenticationConfiguration;

    private final ObjectMapper objectMapper;
    private final LoginFailHandler loginFailHandler;
    private final LoginSuccessHandler loginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 관리자
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 상속으로 새로운 클래스를 사용하기 위해 Config에 정의
    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter() throws Exception {
        LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter(objectMapper);
        loginAuthenticationFilter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        loginAuthenticationFilter.setFilterProcessesUrl("/api/v1/login");  // 로그인 경로 /login -> /api/login 으로 변경
        loginAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler);  // 로그인 성공했을 때 실행시킬 핸들러
        loginAuthenticationFilter.setAuthenticationFailureHandler(loginFailHandler);      // 로그인 실패했을 때 실행시킬 핸들러
        return loginAuthenticationFilter;
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
        http.csrf((csrf) -> csrf.disable());

        // 인증 커스텀 (hasRole : 역할, hasAuthority : 권한)
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers(WHITE_LIST_URL).permitAll()       // 누구나 접근 가능
                .requestMatchers(SWAGGER_LIST_URL).permitAll()       // 누구나 접근 가능
                .anyRequest().authenticated()                // 이외의 경로 모두 인증 필요
        );

        http
                .addFilterAt(loginAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);  // 내가 만든 로그인 필터로 대체(교체)

        return http.build();
    }


}
