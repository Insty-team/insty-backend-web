package insty.payment.kakaopay.dto;

public record KakaoPayReadyRes(

        // 카카오페이 결제 고유 id
        String tid,

        // PC 리다이렉트 URL
        String redirectPcUrl,

        // 모바일 리다이렉트 URL
        String redirectMobileUrl,

        // 앱 리다이렉트 URL
        String redirectAppUrl
) {
    public static KakaoPayReadyRes of(
            String tid,
            String redirectPcUrl,
            String redirectMobileUrl,
            String redirectAppUrl
    ) {
        return new KakaoPayReadyRes(tid, redirectPcUrl, redirectMobileUrl, redirectAppUrl);
    }
}
