package insty.domain.auth.implement;

import insty.constants.JwtValidationType;
import insty.error.CommonErrorCode;
import insty.error.TokenErrorCode;
import insty.exception.CustomException;
import insty.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthTokenValidator {

    private final JwtUtils jwtUtils;

    /**
     * 토큰 검증
     */
    public void validateToken(String token) {
        // 토큰 검증
        JwtValidationType tokenValidStatus = jwtUtils.validateToken(token);
        switch (tokenValidStatus) {
            case VALID -> {} // 정상
            case EXPIRED -> throw new CustomException(TokenErrorCode.ACCESS_TOKEN_EXPIRED); // 만료
            case CLAIMS_INVALID -> throw new CustomException(TokenErrorCode.TOKEN_CLAIMS_INVALID); // 내부 클레임 검증 실패
            case INVALID_SIGNATURE -> throw new CustomException(TokenErrorCode.ACCESS_TOKEN_SIGNATURE_INVALID); // 서명 검증 실패
            case MALFORMED -> throw new CustomException(TokenErrorCode.TOKEN_MALFORMED); // 토큰 형식이 올바르지 않음
            case UNSUPPORTED -> throw new CustomException(TokenErrorCode.TOKEN_UNSUPPORTED); // 지원하지 않음
            default -> throw new CustomException(CommonErrorCode.INTERNAL_ERROR); // 알 수 없는 오류
        }
    }
}
