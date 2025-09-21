package insty.payment.kakaopay.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakaopay")
public record KakaoPayProperties(
        // 카카오페이 API 기본 호스트 (예: https://open-api.kakaopay.com)
        String host,

        // 카카오페이 시크릿 키
        String secretKey,

        // 가맹점 코드 (테스트 CID/실거래 CID 분리)
        String cid,

        // 온라인 결제 API 엔드포인트 경로 모음
        OnlineEndpoints online,

        // HTTP 연결/응답 타임아웃
        Timeout timeout
) {
    public record OnlineEndpoints(
            // 결제 준비 API 경로
            String readyPath,

            // 결제 승인 API 경로
            String approvePath,

            // 주문 조회 API 경로
            String orderPath,

            // 결제 취소 API 경로
            String cancelPath
    ) {}

    public record Timeout(
            // 연결 타임아웃 (ms)
            Integer connectMillis,

            // 응답 타임아웃 (ms)
            Integer readMillis
    ) {}
}
