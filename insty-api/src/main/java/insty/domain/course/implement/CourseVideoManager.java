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

    public UUID attachmentCourse(Course course, UUID videoUuid) {
        if (videoUuid == null) {
            return null;
        }
        VideoCourse videoCourse = videoCourseRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoCourse.updateCourse(course);
        videoCourseRepository.save(videoCourse);
        return videoCourse.getVideoUuid();
    }

    /**
     * 기존 강의영상은 가상삭제하고, 새로운 강의영상을 강의와 연결한다.<br> videoUuid가 null이면 작업을 수행하지 않는다.
     *
     * @param course
     * @param videoUuid
     */
    public UUID updateVideo(Course course, UUID videoUuid) {
        if (videoUuid == null) {
            return null;
        }
        videoCourseRepository.deleteLogicallyByCourseId(course.getId());
        return attachmentCourse(course, videoUuid);
    }

    /**
     * 해당 강의에 연결되어 있는 영상 uuid를 반환한다.<br> 연결된 영상은 is_deleted가 false이다.<br> 연결된 영상이 없으면 null이 반환됨에 주의한다.
     *
     * @param courseId
     * @return
     */
    @Transactional(readOnly = true)
    public UUID getAttachVideoUuid(Long courseId) {
        return videoCourseRepository.findVideoUuidByCourseId(courseId)
                .orElse(null);
    }
}
