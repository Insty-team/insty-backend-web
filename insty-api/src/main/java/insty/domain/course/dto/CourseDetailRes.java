package insty.domain.course.dto;

import insty.domain.common.FileInfo;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import insty.model.video.VideoType;
import java.time.Instant;
import java.util.List;
import java.util.Set;

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

    public static CourseDetailRes from(Course course, List<CourseInstallEnvChecklist> installEnvChecklist,
                                       List<CourseKeypoint> keyPoints, Set<Tags> tags, String thumbnailUrl,
                                       List<FileInfo> practiceFile) {
        List<CourseInstallEnvChecklistInfo> checklistInfos = installEnvChecklist.stream()
                .map(CourseInstallEnvChecklistInfo::from)
                .toList();
        List<String> keyPointContents = keyPoints.stream()
                .map(CourseKeypoint::getContent)
                .toList();
        List<String> tagNames = tags.stream()
                .map(Tags::getTagName)
                .toList();

        return new CourseDetailRes(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTargetAudience(),
                course.getPrice(),
                checklistInfos,
                keyPointContents,
                tagNames,
                VideoType.COURSE,
                thumbnailUrl,
                practiceFile,
                course.getCreatedAt()
        );
    }

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
