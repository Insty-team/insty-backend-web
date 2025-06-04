package insty.domain.course.implement;

import static insty.constants.CourseConstants.COURSE_PRACTICE_FILE_COUNT_LIMIT;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.course.repository.CoursePracticeFileRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.file.implement.FileWriter;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.course.CoursePracticeFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

    /**
     * 기존에 썸네일이 있었다면 지우고 새로 썸네일을 생성한다.<br> 요청이 null 또는 빈값으로 들어오면 교체하지 않는다.
     *
     * @param thumbnail 새로운 썸네일
     * @param course
     * @return 썸네일 url
     */
    public String updateThumbnailAndGetUrl(MultipartFile thumbnail, Course course) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return null;
        }
        File beforeThumbnail = course.getThumbnail();
        if (beforeThumbnail != null) {
            course.updateThumbnail(null);
            courseRepository.save(course);
            fileWriter.deleteFile(beforeThumbnail);
        }

        return saveThumbnailAndGetUrl(thumbnail, course);
    }

    /**
     * 썸네일과 달리 요청된 실습파일만 삭제하고, 새로운 파일 요청이 있다면 추가 생성한다.<br> 파일 개수 제한을 넘기면 에러를 반환한다.
     *
     * @param practiceFiles 새로 추가되는 실습 파일
     * @param deleteFileIds 삭제할 파일 id
     * @param course
     * @return 존재하는 모든 실습파일 정보
     */
    public List<FileInfo> updatePracticeFilesAndGetInfo(List<MultipartFile> practiceFiles, List<Long> deleteFileIds,
                                                        Course course) {
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            coursePracticeFileRepository.deleteByCourseIdAndPracticeFileIdIn(course.getId(), deleteFileIds);
        }
        List<FileInfo> fileInfos = course.getPracticeFiles().stream()
                .map(file -> FileInfo.from(file.getPracticeFile(), appProperties.getDomain()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (practiceFiles == null || practiceFiles.isEmpty()) {
            return fileInfos;
        }
        if (practiceFiles.size() + fileInfos.size() > COURSE_PRACTICE_FILE_COUNT_LIMIT) {
            throw new CustomException(CourseErrorCode.COURSE_TOO_MANY_PRACTICE_FILE);
        }
        fileInfos.addAll(savePracticeFilesAndGetInfo(practiceFiles, course));
        return fileInfos;
    }

    /**
     * 강의와 연관된 모든 썸네일/실습 파일을 s3 및 DB에서 삭제한다.
     *
     * @param courseId
     */
    public void deleteAllFiles(Long courseId) {
        coursePracticeFileRepository.deleteAllByCourseId(courseId);

        fileWriter.deleteAllFile(FileContainerType.COURSE_THUMBNAIL, courseId);
        fileWriter.deleteAllFile(FileContainerType.COURSE_PRACTICE_FILE, courseId);
    }
}
