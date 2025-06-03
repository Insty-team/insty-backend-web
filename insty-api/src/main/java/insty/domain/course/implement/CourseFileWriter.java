package insty.domain.course.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.course.repository.CoursePracticeFileRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.course.CoursePracticeFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
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
    private final CoursePracticeFileRepository coursePracticeFileRepository;
    private final AppProperties appProperties;

    public String saveThumbnailAndGetUrl(MultipartFile thumbnail, Course course) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return null;
        }
        FileCreateReq req = new FileCreateReq(thumbnail, FileContainerType.COURSE_THUMBNAIL, course.getId());
        File file = fileWriter.saveFile(req);
        course.updateThumbnail(file);
        courseRepository.save(course);

        return file.getUrl(appProperties.getDomain());
    }

    public List<FileInfo> savePracticeFilesAndGetInfo(List<MultipartFile> practiceFiles, Course course) {
        if (practiceFiles == null || practiceFiles.isEmpty()) {
            return null;
        }

        List<FileCreateReq> reqs = practiceFiles.stream()
                .map(file -> new FileCreateReq(file, FileContainerType.COURSE_PRACTICE_FILE, course.getId()))
                .toList();
        List<File> files = fileWriter.saveFiles(reqs);
        List<CoursePracticeFile> coursePracticeFiles = files.stream()
                .map(file -> CoursePracticeFile.create(course, file))
                .toList();
        coursePracticeFileRepository.saveAll(coursePracticeFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .toList();
    }
}
