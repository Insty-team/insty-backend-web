package insty.domain.course.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.course.repository.CourseRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseFileWriter {

    private final FileWriter fileWriter;

    private final CourseRepository courseRepository;

    private final AppProperties appProperties;

    public String saveThumbnailAndGetUrl(MultipartFile thumbnail, Course course) {
        FileCreateReq req = new FileCreateReq(thumbnail, FileContainerType.COURSE_THUMBNAIL, course.getId());
        File file = fileWriter.saveFile(req);
        course.updateThumbnail(file);
        courseRepository.save(course);

        return file.getUrl(appProperties.getDomain());
    }
}
