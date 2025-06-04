package insty.domain.course.implement;

import insty.domain.common.FileInfo;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.file.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseFileReader {
    private final AppProperties appProperties;

    public String getThumbnailUrl(Course course) {
        File thumbnail = course.getThumbnail();
        if (thumbnail != null) {
            return thumbnail.getUrl(appProperties.getDomain());
        }
        return null;
    }

    public List<FileInfo> getPracticeFiles(Course course) {
        return course.getPracticeFiles().stream()
                .map(file -> FileInfo.from(file.getPracticeFile(), appProperties.getDomain()))
                .toList();
    }
}
