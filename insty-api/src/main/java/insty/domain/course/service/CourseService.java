package insty.domain.course.service;

import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.dto.CoursePostRes;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseWriter;
import insty.domain.tag.implement.TagWriter;
import insty.model.course.Course;
import insty.model.tag.Tags;
import java.util.List;
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
        courseWriter.saveCourseInstallEnvChecklist(course, req.installEnvChecklist());
        courseWriter.saveCourseKeypoints(course, req.keyPoints());
        List<Tags> tags = tagWriter.saveTags(req.tags());
        courseWriter.saveCourseTags(course, tags);

        List<CourseInstallEnvChecklistInfo> checklists = courseReader.getChecklistsByCourseId(course.getId());
        List<String> keypoints = courseReader.getKeypointContentsByCourseId(course.getId());
        List<String> tagNames = courseReader.getTagNamesByCourseId(course.getId());
        // TODO - 썸네일 url
        return CoursePostRes.from(course, checklists, keypoints, tagNames, null);
    }
}
