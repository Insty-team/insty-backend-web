package insty.payment.kakaopay.config;

import insty.payment.kakaopay.properties.KakaoPayProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoPayProperties.class)
public class KakaoPayRestClientConfig {

    // 카카오페이 API 인증 헤더 키
    private static final String HEADER_AUTHORIZATION = "Authorization";

    // 카카오페이 인증 스킴 접두사
    private static final String AUTH_SCHEME_SECRET_KEY = "SECRET_KEY ";

    // 카카오페이 전용 RestClient Bean 생성
    @Bean(name = "kakaopayRestClient")
    public RestClient kakaopayRestClient(KakaoPayProperties kakaoPayProperties) {
        // KakaoPayProperties 기반으로 타임아웃 설정된 RequestFactory 생성
        ClientHttpRequestFactory clientHttpRequestFactory = buildRequestFactory(kakaoPayProperties);

        // 카카오페이 API 전용 RestClient 빌드
        return RestClient.builder()
                .baseUrl(kakaoPayProperties.host()) // 카카오페이 API 기본 호스트
                .requestFactory(clientHttpRequestFactory) // 타임아웃이 적용된 HTTP 클라이언트
                .defaultHeader(HEADER_AUTHORIZATION, AUTH_SCHEME_SECRET_KEY + kakaoPayProperties.secretKey()) // 인증 헤더
                .defaultHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE) // 기본 Content-Type
                .build();
    }

    // JDK HttpClient 기반 RequestFactory 생성 (연결/응답 타임아웃 적용)
    private ClientHttpRequestFactory buildRequestFactory(KakaoPayProperties kakaoPayProperties) {
        // 연결 타임아웃 (ms), 값이 없으면 기본 3000
        int connectTimeoutMillis = kakaoPayProperties.timeout().connectMillis() != null
                ? kakaoPayProperties.timeout().connectMillis()
                : 3000;

        // 응답 타임아웃 (ms), 값이 없으면 기본 5000
        int readTimeoutMillis = kakaoPayProperties.timeout().readMillis() != null
                ? kakaoPayProperties.timeout().readMillis()
                : 5000;

        // JDK HttpClient 생성 (연결 타임아웃 적용)
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMillis))
                .build();

        // JdkClientHttpRequestFactory 생성 및 응답 타임아웃 설정
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));

        return requestFactory;
    }
}
