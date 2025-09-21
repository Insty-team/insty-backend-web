package insty.payment.kakaopay.dto;

public record KakaoPayApproveReq(

        // 파트너 주문번호
        String orderId,

        // 파트너 사용자 식별자
        String userId,

        // 결제 준비 단계에서 발급받은 tid
        String tid,

        // 카카오 콜백에서 전달받은 pg_token
        String pgToken
) {
    public static KakaoPayApproveReq of(String orderId, String userId, String tid, String pgToken) {
        return new KakaoPayApproveReq(orderId, userId, tid, pgToken);
    }
}
