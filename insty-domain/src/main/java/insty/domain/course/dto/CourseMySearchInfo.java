package insty.domain.course.dto;

import java.time.Instant;
import java.util.List;

public record CourseMySearchInfo(
        Long courseId,
        String title,
        int price,
        int viewCount,
        long commentCount,
        long communityPostCount,
        List<String> tags,
        String thumbnailUrl,
        boolean isShow,
        Instant createdAt
) {

    public static CourseMySearchInfo assembly(CourseMySearchInfo origin, List<String> tags, String thumbnailUrl) {
        return new CourseMySearchInfo(origin.courseId(), origin.title(), origin.price(), origin.viewCount(),
                origin.commentCount(), origin.communityPostCount(), tags, thumbnailUrl, origin.isShow(),
                origin.createdAt());
    }

    public static CourseMySearchInfo setThumbnailUrl(CourseMySearchInfo origin, String thumbnailUrl) {
        return new CourseMySearchInfo(origin.courseId(), origin.title(), origin.price(), origin.viewCount(),
                origin.commentCount(), origin.communityPostCount(), origin.tags(), thumbnailUrl, origin.isShow(),
                origin.createdAt());
    }

    public static CourseMySearchInfo withCommunityPostCount(CourseMySearchInfo origin, long communityPostCount) {
        return new CourseMySearchInfo(origin.courseId(), origin.title(), origin.price(), origin.viewCount(),
                origin.commentCount(), communityPostCount, origin.tags(), origin.thumbnailUrl(), origin.isShow(),
                origin.createdAt());
    }
}
