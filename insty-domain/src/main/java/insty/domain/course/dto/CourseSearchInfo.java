package insty.domain.course.dto;

import java.util.List;

public record CourseSearchInfo(
        Long courseId,
        String title,
        String description,
        List<String> tags,
        String thumbnailUrl,
        String duration // TODO - 영상 길이 추가
) {

    public static CourseSearchInfo assembly(CourseSearchInfo origin, List<String> tags, String thumbnailUrl) {
        return new CourseSearchInfo(origin.courseId(), origin.title(), origin.description(), tags,
                thumbnailUrl, origin.duration());
    }
}
