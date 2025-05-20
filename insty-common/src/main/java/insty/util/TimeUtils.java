package insty.util;

import java.time.Duration;
import java.time.Instant;

public class TimeUtils {

    public static Instant getMinutesLater(long minutes) {
        return Instant.now().plus(Duration.ofMinutes(minutes));
    }
}
