package insty.payment.kakaopay.dto;

// 카카오 Approve API 원문 응답 DTO (내부 전용)
public record KakaoPayApproveDto(

        // 요청 고유 id
        String aid,

        // 승인 완료 시각
        String approvedAt
) {}
