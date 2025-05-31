package insty.domain.course.service;

import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.dto.CoursePostRes;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseWriter;
import insty.domain.tag.implement.TagWriter;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseWriter courseWriter;
    private final CourseReader courseReader;
    private final TagWriter tagWriter;

    public CoursePostRes createCourse(CoursePostReq req, MultipartFile thumbnail, MultipartFile[] practiceFile) {
        // TODO - 썸네일 저장
        // TODO - 실습자료 저장
        Course course = courseWriter.saveCourse(req, null);
        List<CourseInstallEnvChecklist> checklists = courseWriter.saveCourseInstallEnvChecklist(course,
                req.installEnvChecklist());
        List<CourseKeypoint> keypoints = courseWriter.saveCourseKeypoints(course, req.keyPoints());
        Set<Tags> tags = tagWriter.saveTags(req.tags());
        courseWriter.saveCourseTags(course, tags);

        // TODO - 썸네일 url
        return CoursePostRes.from(course, checklists, keypoints, tags, null);
    }
}
