package insty.domain.course.dto;

import java.util.List;

public record CourseSearchInfo(
        Long courseId,
        String title,
        String description,
        List<String> tags,
        String thumbnailUrl,
        Integer durationSecond
) {

    public static CourseSearchInfo assembly(CourseSearchInfo origin, List<String> tags, String thumbnailUrl) {
        return new CourseSearchInfo(origin.courseId(), origin.title(), origin.description(), tags,
                thumbnailUrl, origin.durationSecond());
    }

    public static CourseSearchInfo setThumbnailUrl(CourseSearchInfo origin, String thumbnailUrl) {
        return new CourseSearchInfo(origin.courseId(), origin.title(), origin.description(), origin.tags(),
                thumbnailUrl, origin.durationSecond());
    }
}
