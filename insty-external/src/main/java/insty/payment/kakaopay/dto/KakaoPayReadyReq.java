package insty.payment.kakaopay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record KakaoPayReadyReq(

        // 파트너 주문번호
        @NotBlank
        String orderId,

        // 파트너 사용자 식별자
        @NotBlank
        String userId,

        // 상품명
        @NotBlank
        String itemName,

        // 수량
        @NotNull
        @Positive
        Integer quantity,

        // 총 결제금액
        @NotNull
        @Positive
        Integer totalAmount,

        // 비과세 금액
        @NotNull
        @PositiveOrZero
        Integer taxFreeAmount,

        // 결제성공 리다이렉트 URL
        @NotBlank
        String approvalUrl,

        // 결제취소 리다이렉트 URL (사용자 취소)
        @NotBlank
        String cancelUrl,

        // 결제실패 리다이렉트 URL
        @NotBlank
        String failUrl
) {
    public static KakaoPayReadyReq of(
            String orderId,
            String userId,
            String itemName,
            Integer quantity,
            Integer totalAmount,
            Integer taxFreeAmount,
            String approvalUrl,
            String cancelUrl,
            String failUrl
    ) {
        return new KakaoPayReadyReq(orderId, userId, itemName, quantity,
                totalAmount, taxFreeAmount,
                approvalUrl, cancelUrl, failUrl);
    }
}
