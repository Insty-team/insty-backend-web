package insty.payment.kakaopay.dto;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record KakaoPayApproveRes(
        // 요청 고유 id
        String aid,

        // 승인 완료 시각
        String approvedAt
) {
    public static KakaoPayApproveRes of(String aid, String approvedAt) {
        return new KakaoPayApproveRes(aid, approvedAt);
    }
}