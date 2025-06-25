package insty.domain.course.implement;

import insty.domain.course.dto.CourseRequestReq;
import insty.domain.course.repository.CourseRequestRepository;
import insty.model.course.CourseRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseRequestReader {

    private final CourseRequestRepository courseRequestRepository;

    public List<CourseRequest> getListMyCourseRequest(Long userId) {
        return courseRequestRepository.findByRecipientId(userId);
    }
}
