package insty.domain.course.implement;

import insty.domain.course.repository.CoursePracticeFileRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.domain.file.repository.FileRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.video.VideoCourse;
import insty.model.video.VideoEncoding;
import insty.s3.adapter.S3FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseCleaner {

    private final S3FileManager s3FileManager;

    private final CourseRepository courseRepository;
    private final CourseTagRepository courseTagRepository;
    private final CoursePracticeFileRepository coursePracticeFileRepository;
    private final VideoCourseRepository videoCourseRepository;
    private final VideoEncodingRepository videoEncodingRepository;
    private final FileRepository fileRepository;

    public void cleanAllData(Long userId) {
        List<Long> courseIds = courseRepository.findAllIdByUserId(userId);
        if (courseIds.isEmpty()) {
            return;
        }
        List<String> s3Keys = new ArrayList<>();

        deleteAllTag(courseIds);
        deleteAllFile(s3Keys, courseIds);
        deleteAllVideo(s3Keys, courseIds);
        deleteAllCourse(courseIds);
        s3FileManager.deleteAllByKeyList(s3Keys);
    }

    private void deleteAllTag(List<Long> courseIds) {
        courseTagRepository.deleteAllByCourseIdIn(courseIds);
    }

    private void deleteAllFile(List<String> keys, List<Long> courseIds) {
        List<File> files = fileRepository.findAllByContainerTypeAndContainerIdIn(
                FileContainerType.COURSE_THUMBNAIL, courseIds);
        files.addAll(fileRepository.findAllByContainerTypeAndContainerIdIn(
                FileContainerType.COURSE_PRACTICE_FILE, courseIds));

        for (File file : files) {
            keys.add(getFilePath(file.getContainerType().toString(), file.getContainerId().toString(), file.getName()));
        }
        coursePracticeFileRepository.deleteAllByCourseIdIn(courseIds);
        fileRepository.deleteAll(files);
    }

    private String getFilePath(String directory, String key, String fileName) {
        return "file/" + directory + "/" + key + "/" + fileName;
    }

    private void deleteAllVideo(List<String> keys, List<Long> courseIds) {
        List<VideoCourse> videoCourses = videoCourseRepository.findAllByCourseIdIn(courseIds);
        if (videoCourses.isEmpty()) {
            return;
        }

        List<UUID> videoUuids = videoCourses.stream()
                .map(VideoCourse::getVideoUuid)
                .toList();
        List<VideoEncoding> videoEncodings = videoEncodingRepository.findAllByVideoUuidIn(videoUuids);
        keys.addAll(videoEncodings.stream()
                .map(VideoEncoding::getEncodingVideoDirectoryPath)
                .toList());

        videoEncodingRepository.deleteAll(videoEncodings);
        videoCourseRepository.deleteAll(videoCourses);
        // ai 벡터 업데이트는 ai에서 함께 처리
    }

    private void deleteAllCourse(List<Long> courseIds) {
        courseRepository.deleteAllById(courseIds);
    }
}
