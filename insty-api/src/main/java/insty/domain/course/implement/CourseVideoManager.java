package insty.domain.course.implement;

import insty.domain.video.repository.VideoCourseRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.video.VideoCourse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseVideoManager {

    private final VideoCourseRepository videoCourseRepository;

    public VideoCourse attachmentCourse(Course course, UUID videoUuid) {
        VideoCourse videoCourse = videoCourseRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoCourse.updateCourse(course);
        return videoCourseRepository.save(videoCourse);
    }

    /**
     * 기존 강의영상은 가상삭제하고, 새로운 강의영상을 강의와 연결한다.<br> videoUuid가 null이면 기존에 연결된 강의를 반환한다.
     *
     * @param course
     * @param videoUuid
     */
    public VideoCourse updateAndGetLinkedVideo(Course course, UUID videoUuid) {
        if (videoUuid == null) {
            return getAttachCourseVideo(course.getId());
        }
        videoCourseRepository.deleteLogicallyByCourseId(course.getId());
        return attachmentCourse(course, videoUuid);
    }

    @Transactional(readOnly = true)
    public VideoCourse getAttachCourseVideo(Long courseId) {
        return videoCourseRepository.findByCourseIdAndIsDeleted(courseId, false)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }
}
