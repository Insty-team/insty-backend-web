package insty.payment.kakaopay.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoPayApproveReq(

        // 파트너 주문번호
        @NotBlank
        String orderId,

        // 파트너 사용자 식별자
        @NotBlank
        String userId,

        // 결제 준비 단계에서 발급받은 tid
        @NotBlank
        String tid,

        // 카카오 콜백에서 전달받은 pg_token
        @NotBlank
        String pgToken
) {
    public static KakaoPayApproveReq of(String orderId, String userId, String tid, String pgToken) {
        return new KakaoPayApproveReq(orderId, userId, tid, pgToken);
    }
}
