package insty.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public static Instant getStartOfTodayInKorea() {
        return LocalDate.now(KOREA_ZONE).atStartOfDay(KOREA_ZONE).toInstant();
    }
}
