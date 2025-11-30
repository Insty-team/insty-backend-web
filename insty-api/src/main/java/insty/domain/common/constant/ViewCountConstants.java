package insty.domain.common.constant;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewCountConstants {
    public static final Duration COURSE_VIEW_DUPLICATE_DURATION = Duration.ofHours(18);
}
