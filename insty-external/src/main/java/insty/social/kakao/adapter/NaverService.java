package insty.social.kakao.adapter;

import insty.error.SocialErrorCode;
import insty.exception.CustomException;
import insty.social.kakao.dto.NaverTokenRes;
import insty.social.kakao.dto.NaverUserInfoRes;
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
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class NaverService {

    @Value("${oauth.providers.naver.client-id}")
    private String NAVER_CLIENT_ID;

    @Value("${oauth.providers.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;

    @Value("${oauth.providers.naver.endpoints.auth-url}")
    private String NAVER_AUTH_URL;

    @Value("${oauth.providers.naver.endpoints.token-url}")
    private String NAVER_TOKEN_URL;

    @Value("${oauth.providers.naver.endpoints.user-info-url}")
    private String NAVER_USERINFO_URL;

    @Value("${oauth.providers.naver.endpoints.redirect-url}")
    private String NAVER_REDIRECT_URL;

    private final RestClient restClient;

    /**
     *   인가 코드 받는 URL
     */
    public String getAuthUrl() {
        return UriComponentsBuilder.fromUriString(NAVER_AUTH_URL)
                .queryParam("client_id", NAVER_CLIENT_ID)
                .queryParam("redirect_uri", NAVER_REDIRECT_URL)
                .queryParam("response_type", "code")
                .queryParam("state", "INSTY")
                .build()
                .toUriString();
    }

    /**
     * 카카오 토큰 받기
     */
    public NaverTokenRes getTokenFromNaver(String code) {
        try{
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type",   "authorization_code");
            form.add("client_id",    NAVER_CLIENT_ID);
            form.add("client_secret", NAVER_CLIENT_SECRET);
            form.add("code", code);
            form.add("state", "INSTY");

            return restClient.post()
                    .uri(NAVER_TOKEN_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(form)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NaverTokenRes.class);

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
    public NaverUserInfoRes getUserProfile(String naverToken) {
        try {
            return restClient.get()
                    .uri(NAVER_USERINFO_URL)
                    .headers((header) -> {
                        header.setBearerAuth(naverToken);
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NaverUserInfoRes.class);

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
