package insty.constants;

public enum JwtValidationType {
    VALID,                  // 토큰 유효
    EXPIRED,                // 토큰의 유효기간이 만료
    INVALID_SIGNATURE,      // 서명 검증 실패 (토큰 변조 의심)
    MALFORMED,              // 토큰 형식이 올바르지 않음 (구조적 오류)
    UNSUPPORTED,            // 지원하지 않는 JWT 알고리즘 혹은 타입
    CLAIMS_INVALID,         // 토큰 내부 클레임 검증 실패
    UNKNOWN_ERROR           // 그 외 알 수 없는 오류 발생
}