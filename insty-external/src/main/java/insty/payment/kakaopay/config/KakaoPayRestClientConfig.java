package insty.payment.kakaopay.config;

import insty.payment.kakaopay.properties.KakaoPayProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(KakaoPayProperties.class)
public class KakaoPayRestClientConfig {

    private static final String HEADER_AUTHORIZATION   = "Authorization";
    private static final String HEADER_CONTENT_TYPE    = "Content-Type";
    private static final String AUTH_SCHEME_SECRET_KEY = "SECRET_KEY ";

    // yml에 설정이 따로 되어 있지 않을 경우 사용하는 기본값
    private static final String DEFAULT_CONTENT_TYPE   = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
    private static final String DEFAULT_HOST = "https://open-api.kakaopay.com";
    private static final int DEFAULT_CONNECT_MILLIS = 3000;
    private static final int DEFAULT_READ_MILLIS    = 5000;

    // KakaoPay RestClient 빈 생성
    // yml에 설정이 되어 있지 않아도 기본값으로 작동 (실 결제 연동은 불가능)
    @Bean(name = "kakaopayRestClient")
    public RestClient kakaopayRestClient(KakaoPayProperties kakaopayProperties) {
        // host 값 설정 (미설정 시 기본 호스트 사용)
        String host = resolveHost(kakaopayProperties);

        // 타임아웃 값 설정 (미설정 시 기본값 사용)
        ClientHttpRequestFactory requestFactory = buildRequestFactory(kakaopayProperties);

        // RestClient 빌드 시작: 공통 Content-Type 설정
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(host)
                .requestFactory(requestFactory)
                .defaultHeader(HEADER_CONTENT_TYPE, DEFAULT_CONTENT_TYPE);

        // secretKey가 비어 있으면 헤더 생략 및 경고, 있다면 실제 Authorization 헤더 설정
        String secretKey = (kakaopayProperties != null) ? kakaopayProperties.secretKey() : null;
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("[KakaoPay] secretKey 미설정 상태로 동작합니다. 실제 결제 호출 시 401/권한 오류가 발생할 수 있습니다.");
        } else {
            builder = builder.defaultHeader(HEADER_AUTHORIZATION, AUTH_SCHEME_SECRET_KEY + secretKey);
        }

        // RestClient 인스턴스 생성
        RestClient client = builder.build();

        // 생성 결과 로그 및 리턴(최종 적용된 타임아웃 값 표기)
        log.info("[KakaoPay] RestClient 생성 완료 - baseUrl={}, connectTimeout={}ms, readTimeout={}ms",
                host, resolveConnectTimeout(kakaopayProperties), resolveReadTimeout(kakaopayProperties));
        return client;
    }

    // Timeout에 적용된 값을 사용해 RequestFactory를 생성한다.
    private ClientHttpRequestFactory buildRequestFactory(KakaoPayProperties kakaoPayProperties) {
        // 유효한 타임아웃 ms로 변환 (미설정 시 기본값)
        int connectMs = resolveConnectTimeout(kakaoPayProperties);
        int readMs    = resolveReadTimeout(kakaoPayProperties);

        // JDK HttpClient 생성: 연결 타임아웃 적용
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectMs))
                .build();

        // Spring RequestFactory 생성: 읽기 타임아웃 적용
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readMs));
        return factory;
    }

    // host 값을 최종 확정한다. 만약 yml 값이 비어 있으면 기본 호스트로 대체하고 WARN 로그를 남긴다.
    private String resolveHost(KakaoPayProperties props) {
        String host = (props != null) ? props.host() : null;
        if (host == null || host.isBlank()) {
            log.warn("[KakaoPay] host 미설정 → 기본 host({}) 사용", DEFAULT_HOST);
            return DEFAULT_HOST;
        }
        return host;
    }

    // 연결 타임아웃(ms)을 최종 확정한다. 만약 yml 값이 null/0/음수면 기본값으로 대체하고 WARN 로그를 남긴다.
    private int resolveConnectTimeout(KakaoPayProperties kakaoPayProperties) {
        // null 및 음수, 0값 방어
        if (kakaoPayProperties == null
                || kakaoPayProperties.timeout() == null
                || kakaoPayProperties.timeout().connectMillis() == null
                || kakaoPayProperties.timeout().connectMillis() <= 0) {
            log.warn("[KakaoPay] timeout.connect-millis 미설정 → 기본 {}ms 사용", DEFAULT_CONNECT_MILLIS);
            return DEFAULT_CONNECT_MILLIS;
        }
        return kakaoPayProperties.timeout().connectMillis();
    }

    // 읽기 타임아웃(ms)을 최종 확정한다. 만약 yml 값이 null/0/음수면 기본값으로 대체하고 WARN 로그를 남긴다.
    private int resolveReadTimeout(KakaoPayProperties kakaoPayProperties) {
        if (kakaoPayProperties == null || kakaoPayProperties.timeout() == null || kakaoPayProperties.timeout().readMillis() == null
                || kakaoPayProperties.timeout().readMillis() <= 0) {
            log.warn("[KakaoPay] timeout.read-millis 미설정 → 기본 {}ms 사용", DEFAULT_READ_MILLIS);
            return DEFAULT_READ_MILLIS;
        }
        return kakaoPayProperties.timeout().readMillis();
    }
}
