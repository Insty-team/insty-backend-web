package insty.domain.course.service;

import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.dto.CoursePostRes;
import insty.domain.course.implement.CourseWriter;
import insty.model.course.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseWriter courseWriter;

    public CoursePostRes createCourse(CoursePostReq req, MultipartFile thumbnail, MultipartFile[] practiceFile) {
        // TODO - 썸네일 저장
        // TODO - 실습자료 저장
        Course course = courseWriter.saveCourse(req, null);

        // TODO - 환경 체크리스트
        // TODO - 핵심포인트
        // TODO - 태그
        // TODO - 썸네일 url
        return CoursePostRes.from(course, null, null, null, null);
    }
}
