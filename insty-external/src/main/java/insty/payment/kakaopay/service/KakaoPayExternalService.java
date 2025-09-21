package insty.payment.kakaopay.service;

import insty.payment.kakaopay.dto.KakaoPayApproveDto;
import insty.payment.kakaopay.dto.KakaoPayApproveReq;
import insty.payment.kakaopay.dto.KakaoPayApproveRes;
import insty.payment.kakaopay.dto.KakaoPayReadyDto;
import insty.payment.kakaopay.dto.KakaoPayReadyReq;
import insty.payment.kakaopay.dto.KakaoPayReadyRes;
import insty.payment.kakaopay.properties.KakaoPayProperties;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayExternalService {

    // 카카오 API 폼 파라미터 키
    private static final String KEY_CID = "cid";
    private static final String KEY_PARTNER_ORDER_ID = "partner_order_id";
    private static final String KEY_PARTNER_USER_ID  = "partner_user_id";
    private static final String KEY_ITEM_NAME = "item_name";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_TOTAL_AMOUNT = "total_amount";
    private static final String KEY_TAX_FREE_AMOUNT = "tax_free_amount";
    private static final String KEY_APPROVAL_URL = "approval_url";
    private static final String KEY_CANCEL_URL   = "cancel_url";
    private static final String KEY_FAIL_URL     = "fail_url";
    private static final String KEY_TID = "tid";
    private static final String KEY_PG_TOKEN = "pg_token";

    // 외부 설정 값 (host/secretKey/cid, endpoint path 등)
    private final KakaoPayProperties kakaoPayProperties;

    // 카카오페이 전용 RestClient (Config에서 생성된 Bean, 단 하나 존재)
    private final RestClient restClient;

    // 결제 준비 호출
    public KakaoPayReadyRes requestReady(KakaoPayReadyReq request) {

        // 카카오 규격 form-data 구성
        MultiValueMap<String, String> readyFormData = buildReadyForm(request);

        // Ready API 호출 → 내부 원문 DTO로 수신
        KakaoPayReadyDto rawReady = restClient.post()
                .uri(kakaoPayProperties.online().readyPath())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(readyFormData)
                .retrieve()
                .body(KakaoPayReadyDto.class);

        // 내부 표준 응답 DTO로 변환 (PC/모바일/앱 URL 모두 제공)
        KakaoPayReadyDto nonNullRaw = Objects.requireNonNull(rawReady);
        return KakaoPayReadyRes.of(
                nonNullRaw.tid(),
                nonNullRaw.nextRedirectPcUrl(),
                nonNullRaw.nextRedirectMobileUrl(),
                nonNullRaw.nextRedirectAppUrl()
        );
    }

    // 결제 승인 호출
    public KakaoPayApproveRes requestApprove(KakaoPayApproveReq request) {

        // 카카오 규격 form-data 구성
        MultiValueMap<String, String> approveFormData = buildApproveForm(request);

        // Approve API 호출 → 내부 원문 DTO로 수신
        KakaoPayApproveDto rawApprove = restClient.post()
                .uri(kakaoPayProperties.online().approvePath())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(approveFormData)
                .retrieve()
                .body(KakaoPayApproveDto.class);

        // 내부 표준 응답 DTO로 변환
        KakaoPayApproveDto nonNullRaw = Objects.requireNonNull(rawApprove);
        return KakaoPayApproveRes.of(nonNullRaw.aid(), nonNullRaw.approvedAt());
    }

    // Ready용 form-data 생성
    private MultiValueMap<String, String> buildReadyForm(KakaoPayReadyReq request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(KEY_CID, kakaoPayProperties.cid());
        formData.add(KEY_PARTNER_ORDER_ID, request.orderId());
        formData.add(KEY_PARTNER_USER_ID,  request.userId());
        formData.add(KEY_ITEM_NAME, request.itemName());
        formData.add(KEY_QUANTITY, String.valueOf(request.quantity()));
        formData.add(KEY_TOTAL_AMOUNT, String.valueOf(request.totalAmount()));
        formData.add(KEY_TAX_FREE_AMOUNT, String.valueOf(request.taxFreeAmount()));
        formData.add(KEY_APPROVAL_URL, request.approvalUrl());
        formData.add(KEY_CANCEL_URL,   request.cancelUrl());
        formData.add(KEY_FAIL_URL,     request.failUrl());
        return formData;
    }

    // Approve용 form-data 생성
    private MultiValueMap<String, String> buildApproveForm(KakaoPayApproveReq request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(KEY_CID, kakaoPayProperties.cid());
        formData.add(KEY_TID, request.tid());
        formData.add(KEY_PARTNER_ORDER_ID, request.orderId());
        formData.add(KEY_PARTNER_USER_ID,  request.userId());
        formData.add(KEY_PG_TOKEN,         request.pgToken());
        return formData;
    }
}
