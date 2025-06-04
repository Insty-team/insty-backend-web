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

    public void attachmentCourse(Course course, UUID videoUuid) {
        if (videoUuid == null) {
            return;
        }
        VideoCourse videoCourse = videoCourseRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoCourse.updateCourse(course);
        videoCourseRepository.save(videoCourse);
    }
}
