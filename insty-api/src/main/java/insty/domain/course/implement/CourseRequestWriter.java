package insty.domain.course.implement;

import insty.domain.course.dto.CourseRequestReq;
import insty.domain.course.repository.CourseRequestRepository;
import insty.model.course.CourseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseRequestWriter {

    private final CourseRequestRepository courseRequestRepository;

    public CourseRequest saveCourseRequest(Long userId, CourseRequestReq req) {
        CourseRequest courseRequest = CourseRequest.create(userId, req.title(), req.content(), req.creatorId());
        return courseRequestRepository.save(courseRequest);
    }
}
