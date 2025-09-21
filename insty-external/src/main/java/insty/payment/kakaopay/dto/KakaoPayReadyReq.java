package insty.payment.kakaopay.dto;

// 외부 의존성 없이 수동 검증/기본값 적용하는 Ready 요청 DTO
public record KakaoPayReadyReq(
        // 파트너 주문번호
        String orderId,
        // 파트너 사용자 식별자
        String userId,
        // 상품명
        String itemName,
        // 수량 (양수)
        int quantity,
        // 총 결제금액 (양수)
        int totalAmount,
        // 비과세 금액 (0 이상)
        int taxFreeAmount,
        // 결제성공 리다이렉트 URL
        String approvalUrl,
        // 결제취소 리다이렉트 URL (사용자 취소)
        String cancelUrl,
        // 결제실패 리다이렉트 URL
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
        String normalizedOrderId   = requireNotBlank(orderId,   "orderId");
        String normalizedUserId    = requireNotBlank(userId,    "userId");
        String normalizedItemName  = requireNotBlank(itemName,  "itemName");
        String normalizedApprovalUrl = requireNotBlank(approvalUrl, "approvalUrl");
        String normalizedCancelUrl   = requireNotBlank(cancelUrl,   "cancelUrl");
        String normalizedFailUrl     = requireNotBlank(failUrl,     "failUrl");

        int normalizedQuantity   = requirePositive(quantity,    "quantity");
        int normalizedTotalAmount = requirePositive(totalAmount, "totalAmount");
        int normalizedTaxFreeAmount = normalizeNonNegative(taxFreeAmount);

        // 비과세 금액은 총 결제금액을 초과할 수 없음
        if (normalizedTaxFreeAmount > normalizedTotalAmount) {
            throw new IllegalArgumentException(
                    "taxFreeAmount must be <= totalAmount (taxFreeAmount=" +
                            normalizedTaxFreeAmount + ", totalAmount=" + normalizedTotalAmount + ")"
            );
        }

        return new KakaoPayReadyReq(
                normalizedOrderId,
                normalizedUserId,
                normalizedItemName,
                normalizedQuantity,
                normalizedTotalAmount,
                normalizedTaxFreeAmount,
                normalizedApprovalUrl,
                normalizedCancelUrl,
                normalizedFailUrl
        );
    }

    // 외부 의존성을 가져올 수 없기에, 내부 메서드로 처리

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static int requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    private static int normalizeNonNegative(Integer value) {
        if (value == null) return 0;
        if (value < 0) {
            throw new IllegalArgumentException("taxFreeAmount must be >= 0");
        }
        return value;
    }
}
