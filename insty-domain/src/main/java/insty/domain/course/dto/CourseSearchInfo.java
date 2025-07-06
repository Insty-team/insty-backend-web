package insty.domain.course.dto;

import insty.domain.common.dto.CreatorInfo;
import java.util.List;

public record CourseSearchInfo(
        Long courseId,
        CreatorInfo creatorInfo,
        String title,
        String description,
        List<String> tags,
        String thumbnailUrl,
        Integer durationSecond
) {

    public static CourseSearchInfo assembly(CourseSearchInfo origin, List<String> tags, String thumbnailUrl) {
        return new CourseSearchInfo(origin.courseId(), origin.creatorInfo(), origin.title(), origin.description(), tags,
                thumbnailUrl, origin.durationSecond());
    }

    public static CourseSearchInfo setThumbnailUrl(CourseSearchInfo origin, String thumbnailUrl) {
        return new CourseSearchInfo(origin.courseId(), origin.creatorInfo(), origin.title(), origin.description(),
                origin.tags(),
                thumbnailUrl, origin.durationSecond());
    }
}
