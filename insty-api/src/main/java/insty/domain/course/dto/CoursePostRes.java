package insty.domain.course.dto;

import insty.model.course.Course;
import insty.model.video.VideoType;
import java.time.Instant;
import java.util.List;

public record CoursePostRes(
        Long courseId,
        String title,
        String description,
        String targetAudience,
        int price,
        List<CourseInstallEnvChecklistInfo> installEnvChecklist,
        List<String> keyPoints,
        List<String> tags,
        String thumbnailUrl,
        VideoType videoType,
        // 파일
        Instant createdAt
) {

    public static CoursePostRes from(Course course, List<CourseInstallEnvChecklistInfo> installEnvChecklist,
                                     List<String> keyPoints, List<String> tags, String thumbnailUrl) {
        return new CoursePostRes(course.getId(), course.getTitle(), course.getDescription(), course.getTargetAudience(),
                course.getPrice(), installEnvChecklist, keyPoints, tags, thumbnailUrl, VideoType.COURSE,
                course.getCreatedAt());
    }
}
