package insty.social.kakao.adapter;

import insty.error.SocialErrorCode;
import insty.exception.CustomException;
import insty.social.kakao.dto.GoogleTokenRes;
import insty.social.kakao.dto.GoogleUserInfoRes;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
public class GoogleService {
    @Value("${oauth.providers.google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${oauth.providers.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    @Value("${oauth.providers.google.endpoints.redirect-url}")
    private String GOOGLE_REDIRECT_URL;

    @Value("${oauth.providers.google.endpoints.auth-url}")
    private String GOOGLE_AUTH_URL;

    @Value("${oauth.providers.google.endpoints.token-url}")
    private String GOOGLE_TOKEN_URL;

    @Value("${oauth.providers.google.endpoints.user-info-url}")
    private String GOOGLE_USERINFO_URL;

    private final RestClient restClient;

    /**
     *   인가 코드 받는 URL
     */
    public String getAuthUrl(String state) {
        return UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", GOOGLE_CLIENT_ID)
                .queryParam("redirect_uri", GOOGLE_REDIRECT_URL)
                .queryParam("response_type", "code")
                .queryParam("scope", "email profile openid")
                .queryParam("access_type", "offline")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    /**
     * 구글 토큰 받기
     */
    public GoogleTokenRes getTokenFromGoogle(String code) {
        try{
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type",   "authorization_code");
            form.add("client_id",    GOOGLE_CLIENT_ID);
            form.add("client_secret",    GOOGLE_CLIENT_SECRET);
            form.add("redirect_uri", GOOGLE_REDIRECT_URL);
            form.add("code",         decodingCode(code));

            return restClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(form)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GoogleTokenRes.class);

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
     * 받은 코드 디코딩
     */
    private String decodingCode(String code) {
        return URLDecoder.decode(code, StandardCharsets.UTF_8);
    }

    /**
     * 구글 토큰으로 사용자 정보 조회
     */
    public GoogleUserInfoRes getUserProfile(String googleToken) {
        try {
            return restClient.get()
                    .uri(GOOGLE_USERINFO_URL)
                    .headers((header) -> {
                        header.setBearerAuth(googleToken);
                        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GoogleUserInfoRes.class);

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
