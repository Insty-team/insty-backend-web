package insty.domain.course.implement;

import insty.domain.common.FileInfo;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.file.File;
import insty.util.VideoUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseFileReader {
    private final AppProperties appProperties;

    public String getThumbnailUrl(Course course, UUID videoUuid) {
        File thumbnail = course.getThumbnail();
        if (thumbnail != null) {
            return thumbnail.getUrl(appProperties.getDomain());
        }
        if (videoUuid == null) {
            throw new CustomException(CourseErrorCode.COURSE_NOT_FOUND_LINKED_VIDEO);
        }
        return VideoUtils.getVideoBasicThumbnailUrl(appProperties.getDomain(), videoUuid);
    }

    public List<FileInfo> getPracticeFiles(Course course) {
        return course.getPracticeFiles().stream()
                .map(file -> FileInfo.from(file.getPracticeFile(), appProperties.getDomain()))
                .toList();
    }
}
