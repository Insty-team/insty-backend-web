package insty.domain.course.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.domain.common.dto.CreatorInfo;
import insty.model.course.Course;
import insty.model.user.User;
import insty.model.video.VideoType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseDetailRes(
        Long courseId,
        CreatorInfo creatorInfo,
        String title,
        String description,
        String targetAudience,
        int price,
        Instant createdAt,
        List<CourseInstallEnvChecklistInfo> installEnvChecklist,
        List<String> keyPoints,
        List<String> tags,
        String thumbnailUrl,
        List<FileInfo> practiceFile,
        VideoInfo videoInfo
) {

    public static CourseDetailRes from(Course course, User creator,
                                       List<CourseInstallEnvChecklistInfo> installEnvChecklist,
                                       List<String> keyPoints, List<String> tags, String thumbnailUrl,
                                       List<FileInfo> practiceFile, UUID videoUuid) {
        return new CourseDetailRes(
                course.getId(),
                CreatorInfo.from(creator),
                course.getTitle(),
                course.getDescription(),
                course.getTargetAudience(),
                course.getPrice(),
                course.getCreatedAt(),
                installEnvChecklist,
                keyPoints,
                tags,
                thumbnailUrl,
                practiceFile,
                VideoInfo.of(VideoType.COURSE, videoUuid)
        );
    }
}
