package insty.domain.course.dto;

import insty.domain.common.FileInfo;
import insty.model.course.Course;
import insty.model.video.VideoType;
import java.time.Instant;
import java.util.List;

public record CourseDetailRes(
        Long courseId,
        String title,
        String description,
        String targetAudience,
        int price,
        List<CourseInstallEnvChecklistInfo> installEnvChecklist,
        List<String> keyPoints,
        List<String> tags,
        VideoType videoType,
        String thumbnailUrl,
        List<FileInfo> practiceFile,
        Instant createdAt
) {

    public static CourseDetailRes from(Course course, List<CourseInstallEnvChecklistInfo> installEnvChecklist,
                                       List<String> keyPoints, List<String> tags, String thumbnailUrl,
                                       List<FileInfo> practiceFile) {
        return new CourseDetailRes(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTargetAudience(),
                course.getPrice(),
                installEnvChecklist,
                keyPoints,
                tags,
                VideoType.COURSE,
                thumbnailUrl,
                practiceFile,
                course.getCreatedAt()
        );
    }
}
