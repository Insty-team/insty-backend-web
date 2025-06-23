package insty.constants;

public class JwtConstants {

    public static final long ACCESS_TOKEN_VALIDITY = 6 * 60 * 60 * 1000L;    // 6시간
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L;  // 7일 (ms 단위)
}
