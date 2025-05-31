package insty.domain.course.dto;

import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import insty.model.video.VideoType;
import java.time.Instant;
import java.util.List;
import java.util.Set;

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

    public static CoursePostRes from(Course course, List<CourseInstallEnvChecklist> installEnvChecklist,
                                     List<CourseKeypoint> keyPoints, Set<Tags> tags, String thumbnailUrl) {
        List<CourseInstallEnvChecklistInfo> checklistInfos = installEnvChecklist.stream()
                .map(CourseInstallEnvChecklistInfo::from)
                .toList();
        List<String> keyPointContents = keyPoints.stream()
                .map(CourseKeypoint::getContent)
                .toList();
        List<String> tagNames = tags.stream()
                .map(Tags::getTagName)
                .toList();

        return new CoursePostRes(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTargetAudience(),
                course.getPrice(),
                checklistInfos,
                keyPointContents,
                tagNames,
                thumbnailUrl,
                VideoType.COURSE,
                course.getCreatedAt()
        );
    }

    public static CoursePostRes from(Course course, List<CourseInstallEnvChecklistInfo> installEnvChecklist,
                                     List<String> keyPoints, List<String> tags, String thumbnailUrl) {
        return new CoursePostRes(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTargetAudience(),
                course.getPrice(),
                installEnvChecklist,
                keyPoints,
                tags,
                thumbnailUrl,
                VideoType.COURSE,
                course.getCreatedAt()
        );
    }
}
