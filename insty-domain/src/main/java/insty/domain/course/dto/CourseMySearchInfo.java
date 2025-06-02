package insty.domain.course.dto;

import java.time.Instant;
import java.util.List;

public record CourseMySearchInfo(
        Long courseId,
        String title,
        int price,
        int viewCount,
        Long commentCount,
        List<String> tags,
        String thumbnailUrl,
        boolean isShow,
        Instant createdAt
) {
}
