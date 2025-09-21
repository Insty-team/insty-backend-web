package insty.payment.kakaopay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// 카카오 Ready API 원문 응답 DTO (내부 전용)
public record KakaoPayReadyDto(

        // 카카오페이 결제 고유 id
        String tid,

        // PC 리다이렉트 URL
        @JsonProperty("next_redirect_pc_url")
        String nextRedirectPcUrl,

        // 모바일 리다이렉트 URL
        @JsonProperty("next_redirect_mobile_url")
        String nextRedirectMobileUrl,

        // 앱 리다이렉트 URL
        @JsonProperty("next_redirect_app_url")
        String nextRedirectAppUrl
) {}
