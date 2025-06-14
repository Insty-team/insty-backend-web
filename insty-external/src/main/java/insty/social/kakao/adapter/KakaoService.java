package insty.social.kakao.adapter;

import insty.error.SocialErrorCode;
import insty.exception.CustomException;
import insty.social.kakao.dto.KakaoTokenRes;
import insty.social.kakao.dto.KakaoUserInfoRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${oauth.providers.kakao.client-id}")
    private String KAKAO_CLIENT_ID;

    @Value("${oauth.providers.kakao.endpoints.redirect-url}")
    private String KAKAO_REDIRECT_URL;

    @Value("${oauth.providers.kakao.endpoints.token-url}")
    private String KAKAO_TOKEN_URL;

    @Value("${oauth.providers.kakao.endpoints.user-info-url}")
    private String KAKAO_USERINFO_URL;

    private final RestClient restClient;

    /**
     * 카카오 토큰 받기
     */
    public KakaoTokenRes getTokenFromKakao(String code) {
        try{
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type",   "authorization_code");
            form.add("client_id",    KAKAO_CLIENT_ID);
            form.add("redirect_uri", KAKAO_REDIRECT_URL);
            form.add("code",         code);

            return restClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(form)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(KakaoTokenRes.class);

        } catch (HttpClientErrorException e) {      // TODO 중복 코드 발생 더 좋은 방법이 있을까?
            throw new CustomException(SocialErrorCode.CLIENT_ERROR);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new CustomException(SocialErrorCode.TEMPORARY_SERVER_ERROR);
        } catch (HttpMessageConversionException e) {
            throw new CustomException(SocialErrorCode.MESSAGE_CONVERSION_ERROR);
        } catch (Exception e) {
            throw new CustomException(SocialErrorCode.UNKNOWN_ERROR);
        }

    }

    /**
     * 카카오 토큰으로 사용자 정보 조회
     */
    public KakaoUserInfoRes getUserProfile(String kakaoToken) {
        try {
            return restClient.get()
                    .uri(KAKAO_USERINFO_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .headers((header) -> {
                        header.setBearerAuth(kakaoToken);
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(KakaoUserInfoRes.class);

        } catch (HttpClientErrorException e) {          // TODO 중복 코드 발생 더 좋은 방법이 있을까?
            throw new CustomException(SocialErrorCode.CLIENT_ERROR);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new CustomException(SocialErrorCode.TEMPORARY_SERVER_ERROR);
        } catch (HttpMessageConversionException e) {
            throw new CustomException(SocialErrorCode.MESSAGE_CONVERSION_ERROR);
        } catch (Exception e) {
            throw new CustomException(SocialErrorCode.UNKNOWN_ERROR);
        }

    }
}
