package insty.social.kakao.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class NaverService {

    @Value("${oauth.providers.naver.client-id}")
    private String NAVER_CLIENT_ID;

    @Value("${oauth.providers.naver.endpoints.auth-url}")
    private String NAVER_AUTH_URL;

    @Value("${oauth.providers.naver.endpoints.redirect-url}")
    private String NAVER_REDIRECT_URL;

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
}
