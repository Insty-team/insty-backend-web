package insty.domain.course.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.video.VideoCourse;
import insty.model.video.VideoEncoding;
import insty.s3.adapter.S3FileManager;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseVideoManager {

    private final AiRequester aiRequester;
    private final S3FileManager s3FileManager;

    private final VideoEncodingRepository videoEncodingRepository;
    private final VideoCourseRepository videoCourseRepository;

    public VideoCourse attachmentCourse(Course course, UUID videoUuid) {
        VideoCourse videoCourse = videoCourseRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoCourse.updateCourse(course);
        return videoCourseRepository.save(videoCourse);
    }

    /**
     * 기존 강의영상은 삭제하고, 새로운 강의영상을 강의와 연결한다.<br> videoUuid가 null이면 기존에 연결된 강의를 반환한다.
     *
     * @param course
     * @param videoUuid
     */
    public VideoCourse updateAndGetLinkedVideo(Course course, UUID videoUuid) {
        if (videoUuid == null) {
            return getAttachCourseVideo(course.getId());
        }
        deleteCourseVideo(course.getId());
        return attachmentCourse(course, videoUuid);
    }

    @Transactional(readOnly = true)
    public VideoCourse getAttachCourseVideo(Long courseId) {
        return videoCourseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }

    /**
     * 영상을 찾을 수 없는 경우 작업을 수행하지 않는다.<br> 연결된 강의가 있다면 삭제하고, AI 벡터 업데이트를 위한 API 호출을 진행한다.
     *
     * @param courseId
     */
    public void deleteCourseVideo(Long courseId) {
        Optional<VideoCourse> videoCourse = videoCourseRepository.findByCourseId(courseId);
        if (videoCourse.isEmpty()) {
            return;
        }

        VideoEncoding videoEncoding = videoEncodingRepository.findByVideoUuid(videoCourse.get().getVideoUuid())
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_ENCODING_NOT_FINISHED));
        String directory = videoEncoding.getEncodingVideoDirectoryPath();
        videoCourseRepository.delete(videoCourse.get());
        videoEncodingRepository.delete(videoEncoding);
        aiRequester.deleteAiVideoInfo(videoCourse.get().getVideoUuid());
        s3FileManager.deleteAllByDirectory(directory);
    }
}
