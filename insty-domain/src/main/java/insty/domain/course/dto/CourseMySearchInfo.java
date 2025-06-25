package insty.domain.course.dto;

import java.time.Instant;
import java.util.List;

public record CourseMySearchInfo(
        Long courseId,
        String title,
        int price,
        int viewCount,
        long commentCount,
        List<String> tags,
        String thumbnailUrl,
        boolean isShow,
        Instant createdAt
) {

    public static CourseMySearchInfo assembly(CourseMySearchInfo origin, List<String> tags, String thumbnailUrl) {
        return new CourseMySearchInfo(origin.courseId(), origin.title(), origin.price(), origin.viewCount(),
                origin.commentCount(), tags, thumbnailUrl, origin.isShow(), origin.createdAt());
    }

    public static CourseMySearchInfo setThumbnailUrl(CourseMySearchInfo origin, String thumbnailUrl) {
        return new CourseMySearchInfo(origin.courseId(), origin.title(), origin.price(), origin.viewCount(),
                origin.commentCount(), origin.tags(), thumbnailUrl, origin.isShow(), origin.createdAt());
    }
}
