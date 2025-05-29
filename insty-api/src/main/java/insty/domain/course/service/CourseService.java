package insty.domain.course.service;

import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.dto.CoursePostRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    public CoursePostRes createCourse(CoursePostReq req, MultipartFile thumbnail, MultipartFile[] practiceFile) {
        return null;
    }
}
