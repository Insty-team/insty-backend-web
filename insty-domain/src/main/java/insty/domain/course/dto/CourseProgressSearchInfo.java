package insty.domain.course.dto;

import java.time.Instant;

public record CourseProgressSearchInfo (
        Long courseId,
        String title,
        Long commentCount,
        Long communityPostCount,
        String thumbnailUrl,
        Instant createdAt
){
    public static CourseProgressSearchInfo assembleWithNoUrl(CourseProgressSearchInfo origin, String thumbnailUrl) {
        return new CourseProgressSearchInfo(origin.courseId(), origin.title(), origin.commentCount(),
                origin.communityPostCount(), thumbnailUrl, origin.createdAt());
    }

    public static CourseProgressSearchInfo setThumbnailUrl(CourseProgressSearchInfo origin, String thumbnailUrl) {
        return new CourseProgressSearchInfo(origin.courseId(), origin.title(), origin.commentCount(),
                origin.communityPostCount(), thumbnailUrl, origin.createdAt());
    }

    public static CourseProgressSearchInfo withCommentCount(CourseProgressSearchInfo origin, Long commentCount) {
        return new CourseProgressSearchInfo(origin.courseId(), origin.title(), commentCount,
                origin.communityPostCount(), origin.thumbnailUrl(), origin.createdAt());
    }

    public static CourseProgressSearchInfo withCommunityPostCount(CourseProgressSearchInfo origin, Long communityPostCount) {
        return new CourseProgressSearchInfo(origin.courseId(), origin.title(), origin.commentCount(),
                communityPostCount, origin.thumbnailUrl(), origin.createdAt());
    }
}
